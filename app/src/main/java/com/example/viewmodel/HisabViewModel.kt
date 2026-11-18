package com.example.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupSerializer
import com.example.data.backup.DriveBackupFile
import com.example.data.backup.GoogleDriveBackupManager
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryItem
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.CustomCategory
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.MarketList
import com.example.data.model.MarketListItem
import com.example.data.model.MonthlyBudget
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.PaymentMethod
import com.example.data.model.Receivable
import com.example.data.model.ReceivablePayment
import com.example.data.model.UiState
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.prefs.UserSettings
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MarketListRepository
import com.example.data.repository.SyncSnapshot
import com.example.i18n.AppLanguage
import com.example.i18n.stringsFor
import com.example.notifications.ReminderScheduler
import com.example.ui.theme.AppThemePalette
import com.example.ui.theme.ThemeMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryBreakdown(
  val category: CategoryItem,
  val totalAmount: Double,
  val percentage: Float,
  val transactionCount: Int,
  val budgetLimit: Double,
)

data class MonthlySummary(
  val monthKey: String,
  val totalSpent: Double,
  val totalIncome: Double,
  val netBalance: Double,
  val prevMonthSpent: Double,
  val transactionCount: Int,
  val dailyAverage: Double,
  val topCategory: CategoryBreakdown?,
  val budgetLimit: Double,
  val categoryBreakdowns: List<CategoryBreakdown>,
  val dailyTotals: Map<Int, Double>,
  val sixMonthTotals: List<Pair<String, Double>>,
)

data class AccountBalances(
  val cash: Double = 0.0,
  val bank: Double = 0.0,
  val mobile: Double = 0.0,
  val card: Double = 0.0,
) {
  fun forMethod(method: PaymentMethod): Double = when (method) {
    PaymentMethod.CASH -> cash
    PaymentMethod.BANK -> bank
    PaymentMethod.MOBILE -> mobile
    PaymentMethod.CARD -> card
  }

  val total: Double get() = cash + bank + mobile + card
}

data class DashboardUiData(
  val expenses: List<Expense>,
  val incomes: List<Income>,
  val transfers: List<AccountTransfer> = emptyList(),
  val payables: List<Payable> = emptyList(),
  val payablePayments: List<PayablePayment> = emptyList(),
  val receivables: List<Receivable> = emptyList(),
  val receivablePayments: List<ReceivablePayment> = emptyList(),
  val customCategories: List<CustomCategory>,
  val budgets: List<MonthlyBudget>,
  val hasPendingWrites: Boolean,
  val isFromCache: Boolean,
) {
  fun accountBalances(): AccountBalances {
    fun balanceFor(method: PaymentMethod): Double {
      val inc = incomes.filter { PaymentMethod.fromWire(it.paymentMethod) == method }.sumOf { it.amount }
      val exp = expenses.filter { PaymentMethod.fromWire(it.paymentMethod) == method }.sumOf { it.amount }
      val trIn = transfers.filter { PaymentMethod.fromWire(it.toMethod) == method }.sumOf { it.amount }
      val trOut = transfers.filter { PaymentMethod.fromWire(it.fromMethod) == method }.sumOf { it.amount }
      val borrowed = payables.filter { PaymentMethod.fromWire(it.paymentMethod) == method }.sumOf { it.amount }
      val repaid = payablePayments.filter { PaymentMethod.fromWire(it.paymentMethod) == method }.sumOf { it.amount }
      val lentOut = receivables.filter { PaymentMethod.fromWire(it.paymentMethod) == method }.sumOf { it.amount }
      val collectedBack = receivablePayments.filter { PaymentMethod.fromWire(it.paymentMethod) == method }.sumOf { it.amount }
      return inc - exp + trIn - trOut + borrowed - repaid - lentOut + collectedBack
    }
    return AccountBalances(
      cash = balanceFor(PaymentMethod.CASH),
      bank = balanceFor(PaymentMethod.BANK),
      mobile = balanceFor(PaymentMethod.MOBILE),
      card = balanceFor(PaymentMethod.CARD),
    )
  }

  fun paidAmountForPayable(payable: Payable): Double {
    val matching = payablePayments.filter { it.payableId == payable.id }
    return if (matching.isNotEmpty()) matching.sumOf { it.amount } else payable.paidAmount
  }

  fun totalUnpaidPayables(): Double =
    payables.sumOf { p -> (p.amount - paidAmountForPayable(p)).coerceAtLeast(0.0) }

  fun collectedAmountForReceivable(receivable: Receivable): Double {
    val matching = receivablePayments.filter { it.receivableId == receivable.id }
    return if (matching.isNotEmpty()) matching.sumOf { it.amount } else receivable.collectedAmount
  }

  fun totalUnpaidReceivables(): Double =
    receivables.sumOf { r -> (r.amount - collectedAmountForReceivable(r)).coerceAtLeast(0.0) }
}

private data class WalletOpsSnapshot(
  val transfers: SyncSnapshot<AccountTransfer>,
  val payables: SyncSnapshot<Payable>,
  val payablePayments: SyncSnapshot<PayablePayment>,
  val receivables: SyncSnapshot<Receivable>,
  val receivablePayments: SyncSnapshot<ReceivablePayment>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HisabViewModel(
  private val repository: ExpenseRepository,
  private val marketRepository: MarketListRepository,
  private val prefsRepo: UserPreferencesRepository,
  private val appContext: Context,
  private val currentUserId: String,
  private val currentUserEmail: String = "",
  private val currentUserName: String = "",
) : ViewModel() {

  val settings: StateFlow<UserSettings> = prefsRepo.settingsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserSettings())

  val isOnline: StateFlow<Boolean> = observeConnectivity(appContext)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), checkCurrentOnline(appContext))

  private val _selectedMonthKey = MutableStateFlow(DateFormatUtils.currentMonthKey())
  val selectedMonthKey: StateFlow<String> = _selectedMonthKey.asStateFlow()

  private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
  val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _isLocked = MutableStateFlow(false)
  val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private val _driveFiles = MutableStateFlow<List<DriveBackupFile>>(emptyList())
  val driveFiles: StateFlow<List<DriveBackupFile>> = _driveFiles.asStateFlow()

  private val _selectedMarketListId = MutableStateFlow<String?>(null)
  val selectedMarketListId: StateFlow<String?> = _selectedMarketListId.asStateFlow()

  private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val messages = _messages.asSharedFlow()

  private var pinInitialized = false

  val marketLists: StateFlow<List<MarketList>> = marketRepository
    .observeMarketLists(currentUserId, currentUserEmail)
    .catch { error ->
      val firestoreError = generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
      if (firestoreError == null && error is CancellationException) throw error
      Log.w(TAG, "Error observing market lists", firestoreError ?: error)
      emit(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

  val selectedMarketItems: StateFlow<List<MarketListItem>> = _selectedMarketListId
    .flatMapLatest { listId ->
      if (listId.isNullOrBlank()) {
        flowOf(emptyList())
      } else {
        marketRepository.observeMarketItems(listId, currentUserId, currentUserEmail)
          .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing market items", firestoreError ?: error)
            emit(emptyList())
          }
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

  private val walletOpsFlow = combine(
    repository.observeTransfers(currentUserId),
    repository.observePayables(currentUserId),
    repository.observePayablePayments(currentUserId),
    repository.observeReceivables(currentUserId),
    repository.observeReceivablePayments(currentUserId),
  ) { trSnap, paySnap, pmtSnap, recSnap, recPmtSnap ->
    WalletOpsSnapshot(trSnap, paySnap, pmtSnap, recSnap, recPmtSnap)
  }

  val dashboardState: StateFlow<UiState<DashboardUiData>> = combine(
    repository.observeExpenses(currentUserId),
    repository.observeIncomes(currentUserId),
    walletOpsFlow,
    repository.observeCustomCategories(currentUserId),
    repository.observeBudgets(currentUserId),
  ) { expSnap: SyncSnapshot<Expense>, incSnap: SyncSnapshot<Income>, walletOps, cats: List<CustomCategory>, budgets: List<MonthlyBudget> ->
    val (trSnap, paySnap, pmtSnap, recSnap, recPmtSnap) = walletOps
    UiState.Success(
      DashboardUiData(
        expenses = expSnap.items,
        incomes = incSnap.items,
        transfers = trSnap.items,
        payables = paySnap.items,
        payablePayments = pmtSnap.items,
        receivables = recSnap.items,
        receivablePayments = recPmtSnap.items,
        customCategories = cats,
        budgets = budgets,
        hasPendingWrites = expSnap.hasPendingWrites ||
          incSnap.hasPendingWrites ||
          trSnap.hasPendingWrites ||
          paySnap.hasPendingWrites ||
          pmtSnap.hasPendingWrites ||
          recSnap.hasPendingWrites ||
          recPmtSnap.hasPendingWrites,
        isFromCache = expSnap.isFromCache &&
          incSnap.isFromCache &&
          trSnap.isFromCache &&
          paySnap.isFromCache &&
          pmtSnap.isFromCache &&
          recSnap.isFromCache &&
          recPmtSnap.isFromCache,
      )
    ) as UiState<DashboardUiData>
  }
    .catch { error ->
      val firestoreError = generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
      if (firestoreError == null && error is CancellationException) throw error
      val rootError = firestoreError ?: error
      Log.w(TAG, "Error observing user expenses", rootError)
      emit(UiState.Error(rootError.localizedMessage ?: "Failed to load expenses"))
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
      initialValue = UiState.Loading,
    )

  init {
    viewModelScope.launch {
      prefsRepo.settingsFlow.collect { userSettings ->
        if (!pinInitialized) {
          pinInitialized = true
          if (userSettings.pinEnabled && userSettings.pinHash.isNotEmpty()) {
            _isLocked.value = true
          }
          // Apply cloud sync network toggle on startup
          repository.setCloudSyncEnabled(userSettings.cloudSyncEnabled)
        }
      }
    }
  }

  fun onAppBackgrounded() {
    val s = settings.value
    if (s.pinEnabled && s.lockOnBackground && s.pinHash.isNotEmpty()) {
      _isLocked.value = true
    }
  }

  fun unlockWithPin(pin: String): Boolean {
    val valid = prefsRepo.verifyPin(pin, settings.value.pinHash)
    if (valid) {
      _isLocked.value = false
    }
    return valid
  }

  fun enablePin(pin: String) {
    viewModelScope.launch {
      prefsRepo.enablePin(pin)
      _messages.tryEmit(stringsFor(settings.value.language).pinSaved)
    }
  }

  fun disablePin() {
    viewModelScope.launch {
      prefsRepo.disablePin()
      _isLocked.value = false
    }
  }

  fun setLockOnBackground(enabled: Boolean) {
    viewModelScope.launch { prefsRepo.setLockOnBackground(enabled) }
  }

  fun selectMonth(monthKey: String) {
    _selectedMonthKey.value = monthKey
  }

  fun shiftMonth(delta: Int) {
    _selectedMonthKey.value = DateFormatUtils.shiftMonthKey(_selectedMonthKey.value, delta)
  }

  fun setCategoryFilter(categoryId: String?) {
    _selectedCategoryFilter.value = categoryId
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch {
      prefsRepo.setLanguage(language)
      val s = settings.value
      if (s.dailyReminderEnabled) {
        ReminderScheduler.scheduleDailyReminder(
          appContext,
          enabled = true,
          hour = s.reminderHour,
          minute = s.reminderMinute,
          langCode = language.code,
        )
      }
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    viewModelScope.launch { prefsRepo.setThemeMode(mode) }
  }

  fun setThemePalette(palette: AppThemePalette) {
    viewModelScope.launch { prefsRepo.setThemePalette(palette) }
  }

  fun setCurrencyCode(code: String) {
    viewModelScope.launch { prefsRepo.setCurrencyCode(code) }
  }

  fun setCloudSyncEnabled(enabled: Boolean) {
    viewModelScope.launch {
      prefsRepo.setCloudSyncEnabled(enabled)
      repository.setCloudSyncEnabled(enabled)
    }
  }

  fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
    viewModelScope.launch {
      prefsRepo.setDailyReminder(enabled, hour, minute)
      ReminderScheduler.scheduleDailyReminder(
        appContext,
        enabled = enabled,
        hour = hour,
        minute = minute,
        langCode = settings.value.language.code,
      )
    }
  }

  fun setBudgetAlertsEnabled(enabled: Boolean) {
    viewModelScope.launch { prefsRepo.setBudgetAlertsEnabled(enabled) }
  }

  fun addExpense(
    title: String,
    amount: Double,
    categoryId: String,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      val result = repository.addExpense(
        userId = currentUserId,
        title = title,
        amount = amount,
        categoryId = categoryId,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
      )
      result.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to save expense")
      }.onSuccess {
        checkBudgetThresholds(DateFormatUtils.monthKeyFromMillis(dateMillis), extraAmount = 0.0)
      }
    }
  }

  fun updateExpense(
    expenseId: String,
    title: String,
    amount: Double,
    categoryId: String,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      val result = repository.updateExpense(
        userId = currentUserId,
        expenseId = expenseId,
        title = title,
        amount = amount,
        categoryId = categoryId,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
      )
      result.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to update expense")
      }
    }
  }

  fun deleteExpense(expenseId: String) {
    viewModelScope.launch {
      val result = repository.deleteExpense(currentUserId, expenseId)
      result.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to delete expense")
      }
    }
  }

  fun addIncome(
    title: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      val result = repository.addIncome(
        userId = currentUserId,
        title = title,
        amount = amount,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
      )
      result.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to save balance")
      }
    }
  }

  fun updateIncome(
    incomeId: String,
    title: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      val result = repository.updateIncome(
        userId = currentUserId,
        incomeId = incomeId,
        title = title,
        amount = amount,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
      )
      result.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to update balance")
      }
    }
  }

  fun deleteIncome(incomeId: String) {
    viewModelScope.launch {
      val result = repository.deleteIncome(currentUserId, incomeId)
      result.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to delete balance")
      }
    }
  }

  fun addTransfer(
    fromMethod: PaymentMethod,
    toMethod: PaymentMethod,
    amount: Double,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      repository.addTransfer(
        userId = currentUserId,
        fromMethod = fromMethod,
        toMethod = toMethod,
        amount = amount,
        note = note,
        dateMillis = dateMillis,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to transfer funds")
      }
    }
  }

  fun updateTransfer(
    transferId: String,
    fromMethod: PaymentMethod,
    toMethod: PaymentMethod,
    amount: Double,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      repository.updateTransfer(
        userId = currentUserId,
        transferId = transferId,
        fromMethod = fromMethod,
        toMethod = toMethod,
        amount = amount,
        note = note,
        dateMillis = dateMillis,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to update transfer")
      }
    }
  }

  fun deleteTransfer(transferId: String) {
    viewModelScope.launch {
      repository.deleteTransfer(currentUserId, transferId).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to delete transfer")
      }
    }
  }

  fun addPayable(
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      repository.addPayable(
        userId = currentUserId,
        personName = personName,
        amount = amount,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to add borrowed record")
      }
    }
  }

  fun updatePayable(
    payable: Payable,
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      repository.updatePayable(
        userId = currentUserId,
        payable = payable,
        personName = personName,
        amount = amount,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to update payable")
      }
    }
  }

  fun deletePayable(payableId: String) {
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val relatedPaymentIds = currentData?.payablePayments
      ?.filter { it.payableId == payableId }
      ?.map { it.id }
      .orEmpty()
    viewModelScope.launch {
      repository.deletePayable(currentUserId, payableId, relatedPaymentIds).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to delete payable")
      }
    }
  }

  fun repayPayable(
    payable: Payable,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val currentPaid = currentData?.paidAmountForPayable(payable) ?: payable.paidAmount
    viewModelScope.launch {
      repository.addPayablePayment(
        userId = currentUserId,
        payable = payable,
        amount = amount,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
        currentPaidSum = currentPaid,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to record repayment")
      }
    }
  }

  fun deletePayablePayment(payment: PayablePayment) {
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val parent = currentData?.payables?.firstOrNull { it.id == payment.payableId }
    val newPaidSum = currentData?.payablePayments
      ?.filter { it.payableId == payment.payableId && it.id != payment.id }
      ?.sumOf { it.amount }
      ?: ((parent?.paidAmount ?: 0.0) - payment.amount).coerceAtLeast(0.0)
    viewModelScope.launch {
      repository.deletePayablePayment(
        userId = currentUserId,
        payment = payment,
        parentPayable = parent,
        newPaidSumAfterDelete = newPaidSum,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to delete repayment")
      }
    }
  }

  fun addReceivable(
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      repository.addReceivable(
        userId = currentUserId,
        personName = personName,
        amount = amount,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to add lent record")
      }
    }
  }

  fun updateReceivable(
    receivable: Receivable,
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    viewModelScope.launch {
      repository.updateReceivable(
        userId = currentUserId,
        receivable = receivable,
        personName = personName,
        amount = amount,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to update receivable")
      }
    }
  }

  fun deleteReceivable(receivableId: String) {
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val relatedPaymentIds = currentData?.receivablePayments
      ?.filter { it.receivableId == receivableId }
      ?.map { it.id }
      .orEmpty()
    viewModelScope.launch {
      repository.deleteReceivable(currentUserId, receivableId, relatedPaymentIds).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to delete receivable")
      }
    }
  }

  fun collectReceivable(
    receivable: Receivable,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) {
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val currentCollected = currentData?.collectedAmountForReceivable(receivable) ?: receivable.collectedAmount
    viewModelScope.launch {
      repository.addReceivablePayment(
        userId = currentUserId,
        receivable = receivable,
        amount = amount,
        paymentMethod = paymentMethod,
        note = note,
        dateMillis = dateMillis,
        currentCollectedSum = currentCollected,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to record collection")
      }
    }
  }

  fun deleteReceivablePayment(payment: ReceivablePayment) {
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val parent = currentData?.receivables?.firstOrNull { it.id == payment.receivableId }
    val newCollectedSum = currentData?.receivablePayments
      ?.filter { it.receivableId == payment.receivableId && it.id != payment.id }
      ?.sumOf { it.amount }
      ?: ((parent?.collectedAmount ?: 0.0) - payment.amount).coerceAtLeast(0.0)
    viewModelScope.launch {
      repository.deleteReceivablePayment(
        userId = currentUserId,
        payment = payment,
        parentReceivable = parent,
        newCollectedSumAfterDelete = newCollectedSum,
      ).onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to delete collection")
      }
    }
  }

  fun selectMarketList(listId: String?) {
    _selectedMarketListId.value = listId
  }

  fun createMarketList(
    title: String,
    sharedWithEmail: String,
    note: String,
  ) {
    viewModelScope.launch {
      marketRepository.createMarketList(
        ownerUid = currentUserId,
        ownerEmail = currentUserEmail,
        ownerName = currentUserName,
        title = title,
        sharedWithEmail = sharedWithEmail,
        note = note,
      ).onSuccess { newId ->
        _selectedMarketListId.value = newId
      }.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to create market list")
      }
    }
  }

  fun updateMarketListAndShare(
    list: MarketList,
    title: String,
    sharedWithEmail: String,
    note: String,
  ) {
    viewModelScope.launch {
      marketRepository.updateMarketListAndShare(
        list = list,
        title = title,
        sharedWithEmail = sharedWithEmail,
        note = note,
      ).onSuccess {
        _messages.tryEmit(stringsFor(settings.value.language).sharedListSavedMsg)
      }.onFailure { err ->
        _messages.tryEmit(err.localizedMessage ?: "Failed to update market list")
      }
    }
  }

  fun toggleMarketListCompleted(listId: String, completed: Boolean) {
    viewModelScope.launch {
      marketRepository.toggleMarketListCompleted(listId, completed)
        .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to update status") }
    }
  }

  fun deleteMarketList(list: MarketList) {
    viewModelScope.launch {
      marketRepository.deleteMarketList(list.ownerUid, list.id)
        .onSuccess {
          if (_selectedMarketListId.value == list.id) {
            _selectedMarketListId.value = null
          }
        }
        .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to delete market list") }
    }
  }

  fun addMarketItem(
    list: MarketList,
    name: String,
    quantity: String,
    estimatedPrice: Double,
    categoryId: String = "groceries",
    note: String = "",
  ) {
    viewModelScope.launch {
      marketRepository.addMarketItem(
        list = list,
        addedByEmail = currentUserEmail,
        name = name,
        quantity = quantity,
        estimatedPrice = estimatedPrice,
        categoryId = categoryId,
        note = note,
      ).onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to add item") }
    }
  }

  fun updateMarketItem(
    listId: String,
    item: MarketListItem,
    name: String,
    quantity: String,
    estimatedPrice: Double,
    categoryId: String = item.categoryId,
    bought: Boolean,
    note: String = item.note,
  ) {
    viewModelScope.launch {
      marketRepository.updateMarketItem(
        listId = listId,
        itemId = item.id,
        name = name,
        quantity = quantity,
        estimatedPrice = estimatedPrice,
        categoryId = categoryId,
        bought = bought,
        note = note,
      ).onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to update item") }
    }
  }

  fun toggleMarketItemBought(listId: String, item: MarketListItem) {
    updateMarketItem(
      listId = listId,
      item = item,
      name = item.name,
      quantity = item.quantity,
      estimatedPrice = item.estimatedPrice,
      categoryId = item.categoryId,
      bought = !item.bought,
      note = item.note,
    )
  }

  fun deleteMarketItem(listId: String, itemId: String) {
    viewModelScope.launch {
      marketRepository.deleteMarketItem(listId, itemId)
        .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to delete item") }
    }
  }

  fun logMarketListAsExpense(
    list: MarketList,
    totalAmount: Double,
    paymentMethod: PaymentMethod,
    categoryId: String = "groceries",
    splitByCategory: Boolean = false,
    markCompleted: Boolean = true,
  ) {
    val strings = stringsFor(settings.value.language)
    val allItems = selectedMarketItems.value
    val activeItems = allItems.filter { it.bought }.ifEmpty { allItems }
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val mergedCategories = CategoryCatalog.mergeCategories(strings, currentData?.customCategories.orEmpty())
      .associateBy { it.id }

    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val pricedBuckets = activeItems
        .filter { it.estimatedPrice > 0.0 }
        .groupBy { it.categoryId.ifBlank { "groceries" } }

      if (splitByCategory && pricedBuckets.size > 1) {
        var allSucceeded = true
        pricedBuckets.forEach { (catId, bucketItems) ->
          val bucketSum = bucketItems.sumOf { it.estimatedPrice }
          val catName = mergedCategories[catId]?.name ?: catId
          val noteSummary = bucketItems.take(6).joinToString(", ") { it.name }
          val res = repository.addExpense(
            userId = currentUserId,
            title = "${list.title} ($catName)",
            amount = bucketSum,
            categoryId = catId,
            paymentMethod = paymentMethod,
            note = noteSummary,
            dateMillis = now,
          )
          if (res.isFailure) {
            allSucceeded = false
            res.exceptionOrNull()?.let { err ->
              _messages.tryEmit(err.localizedMessage ?: "Failed to save expense")
            }
          }
        }
        if (allSucceeded) {
          if (markCompleted && !list.completed) {
            marketRepository.toggleMarketListCompleted(list.id, true)
          }
          checkBudgetThresholds(DateFormatUtils.monthKeyFromMillis(now), extraAmount = 0.0)
          _messages.tryEmit(strings.marketSplitExpenseSaved)
        }
      } else {
        val itemsSummary = activeItems.take(6).joinToString(", ") { it.name }
        val expRes = repository.addExpense(
          userId = currentUserId,
          title = list.title,
          amount = totalAmount,
          categoryId = categoryId.ifBlank { "groceries" },
          paymentMethod = paymentMethod,
          note = itemsSummary,
          dateMillis = now,
        )
        expRes.onSuccess {
          if (markCompleted && !list.completed) {
            marketRepository.toggleMarketListCompleted(list.id, true)
          }
          checkBudgetThresholds(DateFormatUtils.monthKeyFromMillis(now), extraAmount = 0.0)
          _messages.tryEmit(strings.marketExpenseSaved)
        }.onFailure { err ->
          _messages.tryEmit(err.localizedMessage ?: "Failed to save bazaar expense")
        }
      }
    }
  }

  fun addCustomCategory(name: String, iconKey: String, colorHex: Long) {
    viewModelScope.launch {
      repository.addCustomCategory(currentUserId, name, iconKey, colorHex)
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to add category") }
    }
  }

  fun deleteCustomCategory(categoryId: String) {
    viewModelScope.launch {
      repository.deleteCustomCategory(currentUserId, categoryId)
        .onFailure { _messages.tryEmit(it.localizedMessage ?: "Failed to delete category") }
    }
  }

  fun saveBudget(
    monthKey: String,
    totalLimit: Double,
    categoryLimits: Map<String, Double>,
  ) {
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val alreadyExists = currentData?.budgets?.any { it.monthKey == monthKey } == true
    val currency = settings.value.currencyCode
    viewModelScope.launch {
      repository.saveMonthlyBudget(
        userId = currentUserId,
        monthKey = monthKey,
        totalLimit = totalLimit,
        categoryLimits = categoryLimits,
        currencyCode = currency,
        alreadyExists = alreadyExists,
      ).onSuccess {
        _messages.tryEmit(stringsFor(settings.value.language).budgetSaved)
      }.onFailure {
        _messages.tryEmit(it.localizedMessage ?: "Failed to save budget")
      }
    }
  }

  fun buildJsonBackupString(): String {
    val data = (dashboardState.value as? UiState.Success)?.data
    return BackupSerializer.toJsonBackup(
      expenses = data?.expenses.orEmpty(),
      customCategories = data?.customCategories.orEmpty(),
      budgets = data?.budgets.orEmpty(),
      incomes = data?.incomes.orEmpty(),
      transfers = data?.transfers.orEmpty(),
      payables = data?.payables.orEmpty(),
      payablePayments = data?.payablePayments.orEmpty(),
      receivables = data?.receivables.orEmpty(),
      receivablePayments = data?.receivablePayments.orEmpty(),
    )
  }

  fun buildCsvExportString(): String {
    val data = (dashboardState.value as? UiState.Success)?.data
    val strings = stringsFor(settings.value.language)
    val categories = CategoryCatalog.mergeCategories(strings, data?.customCategories.orEmpty())
      .associateBy { it.id }
    val symbol = CurrencyOption.fromCode(settings.value.currencyCode).symbol
    return BackupSerializer.toCsvExport(
      expenses = data?.expenses.orEmpty(),
      categoriesById = categories,
      currencySymbol = symbol,
      strings = strings,
      langCode = settings.value.language.code,
      incomes = data?.incomes.orEmpty(),
      transfers = data?.transfers.orEmpty(),
      payables = data?.payables.orEmpty(),
      payablePayments = data?.payablePayments.orEmpty(),
      receivables = data?.receivables.orEmpty(),
      receivablePayments = data?.receivablePayments.orEmpty(),
    )
  }

  fun importJsonBackup(jsonText: String) {
    val currentData = (dashboardState.value as? UiState.Success)?.data
    val existingCatIds = currentData?.customCategories?.map { it.id }?.toSet().orEmpty()
    val existingBudgetIds = currentData?.budgets?.map { it.monthKey }?.toSet().orEmpty()
    viewModelScope.launch {
      _isBusy.value = true
      try {
        val parsed = BackupSerializer.parseJsonBackup(jsonText)
        val res = repository.importBackup(
          userId = currentUserId,
          expenses = parsed.expenses,
          customCategories = parsed.customCategories,
          budgets = parsed.budgets,
          existingCategoryIds = existingCatIds,
          existingBudgetIds = existingBudgetIds,
          incomes = parsed.incomes,
          transfers = parsed.transfers,
          payables = parsed.payables,
          payablePayments = parsed.payablePayments,
          receivables = parsed.receivables,
          receivablePayments = parsed.receivablePayments,
        )
        res.onSuccess {
          _messages.tryEmit(stringsFor(settings.value.language).importSuccess)
        }.onFailure {
          _messages.tryEmit(it.localizedMessage ?: "Import failed")
        }
      } catch (e: Exception) {
        _messages.tryEmit(e.localizedMessage ?: "Invalid backup file")
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun uploadBackupToDrive(accessToken: String) {
    viewModelScope.launch {
      _isBusy.value = true
      try {
        val json = buildJsonBackupString()
        val result = GoogleDriveBackupManager.uploadBackup(accessToken, json)
        result.onSuccess {
          prefsRepo.setLastDriveBackupMillis(System.currentTimeMillis())
          _messages.tryEmit(stringsFor(settings.value.language).driveBackupSuccess)
        }.onFailure { err ->
          _messages.tryEmit(err.localizedMessage ?: "Google Drive upload failed")
        }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun fetchDriveBackups(accessToken: String, onReady: () -> Unit) {
    viewModelScope.launch {
      _isBusy.value = true
      try {
        val result = GoogleDriveBackupManager.listBackups(accessToken)
        result.onSuccess { files ->
          _driveFiles.value = files
          onReady()
        }.onFailure { err ->
          _messages.tryEmit(err.localizedMessage ?: "Failed to list Drive backups")
        }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun restoreFromDriveFile(accessToken: String, fileId: String) {
    viewModelScope.launch {
      _isBusy.value = true
      try {
        val download = GoogleDriveBackupManager.downloadBackup(accessToken, fileId)
        download.onSuccess { jsonText ->
          val currentData = (dashboardState.value as? UiState.Success)?.data
          val parsed = BackupSerializer.parseJsonBackup(jsonText)
          val res = repository.importBackup(
            userId = currentUserId,
            expenses = parsed.expenses,
            customCategories = parsed.customCategories,
            budgets = parsed.budgets,
            existingCategoryIds = currentData?.customCategories?.map { it.id }?.toSet().orEmpty(),
            existingBudgetIds = currentData?.budgets?.map { it.monthKey }?.toSet().orEmpty(),
            incomes = parsed.incomes,
            transfers = parsed.transfers,
            payables = parsed.payables,
            payablePayments = parsed.payablePayments,
            receivables = parsed.receivables,
            receivablePayments = parsed.receivablePayments,
          )
          res.onSuccess {
            _messages.tryEmit(stringsFor(settings.value.language).driveRestoreSuccess)
          }.onFailure {
            _messages.tryEmit(it.localizedMessage ?: "Restore failed")
          }
        }.onFailure { err ->
          _messages.tryEmit(err.localizedMessage ?: "Drive download failed")
        }
      } catch (e: Exception) {
        _messages.tryEmit(e.localizedMessage ?: "Invalid Drive backup")
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun notifyMessage(msg: String) {
    _messages.tryEmit(msg)
  }

  private fun checkBudgetThresholds(monthKey: String, extraAmount: Double) {
    val s = settings.value
    if (!s.budgetAlertsEnabled) return
    val data = (dashboardState.value as? UiState.Success)?.data ?: return
    val budget = data.budgets.firstOrNull { it.monthKey == monthKey }
      ?: data.budgets.firstOrNull { it.monthKey == "default" }
      ?: return
    if (budget.totalLimit <= 0.0) return

    val spent = data.expenses.filter { it.monthKey == monthKey }.sumOf { it.amount } + extraAmount
    val ratio = spent / budget.totalLimit
    val strings = stringsFor(s.language)
    val symbol = CurrencyOption.fromCode(s.currencyCode).symbol
    if (ratio >= 1.0) {
      ReminderScheduler.showBudgetAlertNotification(
        appContext,
        title = strings.budgetAlert100Title,
        body = "${CurrencyFormat.format(spent, symbol)} / ${CurrencyFormat.format(budget.totalLimit, symbol)}",
      )
    } else if (ratio >= 0.8) {
      ReminderScheduler.showBudgetAlertNotification(
        appContext,
        title = strings.budgetAlert80Title,
        body = "${CurrencyFormat.format(spent, symbol)} / ${CurrencyFormat.format(budget.totalLimit, symbol)}",
      )
    }
  }

  fun computeMonthlySummary(
    data: DashboardUiData,
    monthKey: String,
    language: AppLanguage,
  ): MonthlySummary {
    val strings = stringsFor(language)
    val categories = CategoryCatalog.mergeCategories(strings, data.customCategories)
    val catMap = categories.associateBy { it.id }

    val monthExpenses = data.expenses.filter { it.monthKey == monthKey }
    val monthIncomes = data.incomes.filter { it.monthKey == monthKey }
    val prevMonthKey = DateFormatUtils.shiftMonthKey(monthKey, -1)
    val prevExpenses = data.expenses.filter { it.monthKey == prevMonthKey }

    val totalSpent = monthExpenses.sumOf { it.amount }
    val totalIncome = monthIncomes.sumOf { it.amount }
    val prevSpent = prevExpenses.sumOf { it.amount }

    val budgetDoc = data.budgets.firstOrNull { it.monthKey == monthKey }
      ?: data.budgets.firstOrNull { it.monthKey == "default" }
    val budgetLimit = budgetDoc?.totalLimit ?: 0.0
    val catLimits = budgetDoc?.categoryLimits.orEmpty()

    val groupedByCat = monthExpenses.groupBy { it.categoryId }
    val breakdowns = groupedByCat.map { (catId, list) ->
      val cat = catMap[catId] ?: CategoryItem(catId, catId, "other", 0xFF0891B2)
      val sum = list.sumOf { it.amount }
      val pct = if (totalSpent > 0) (sum / totalSpent).toFloat() else 0f
      CategoryBreakdown(
        category = cat,
        totalAmount = sum,
        percentage = pct,
        transactionCount = list.size,
        budgetLimit = catLimits[catId] ?: 0.0,
      )
    }.sortedByDescending { it.totalAmount }

    val dailyTotals = mutableMapOf<Int, Double>()
    val cal = Calendar.getInstance()
    for (exp in monthExpenses) {
      cal.timeInMillis = exp.dateMillis
      val day = cal.get(Calendar.DAY_OF_MONTH)
      dailyTotals[day] = (dailyTotals[day] ?: 0.0) + exp.amount
    }

    val daysCount = DateFormatUtils.daysInMonth(monthKey)
    val activeDays = if (monthKey == DateFormatUtils.currentMonthKey()) {
      Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    } else {
      daysCount
    }
    val dailyAvg = if (totalSpent > 0) totalSpent / activeDays else 0.0

    val sixMonthTotals = (-5..0).map { offset ->
      val mk = DateFormatUtils.shiftMonthKey(monthKey, offset)
      val sum = data.expenses.filter { it.monthKey == mk }.sumOf { it.amount }
      DateFormatUtils.formatShortMonthLabel(mk, language.code) to sum
    }

    return MonthlySummary(
      monthKey = monthKey,
      totalSpent = totalSpent,
      totalIncome = totalIncome,
      netBalance = totalIncome - totalSpent,
      prevMonthSpent = prevSpent,
      transactionCount = monthExpenses.size,
      dailyAverage = dailyAvg,
      topCategory = breakdowns.firstOrNull(),
      budgetLimit = budgetLimit,
      categoryBreakdowns = breakdowns,
      dailyTotals = dailyTotals,
      sixMonthTotals = sixMonthTotals,
    )
  }

  private fun checkCurrentOnline(context: Context): Boolean {
    return try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
      val active = cm.activeNetwork ?: return false
      val caps = cm.getNetworkCapabilities(active) ?: return false
      caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) {
      Log.w(TAG, "Connectivity check failed", e)
      false
    }
  }

  private fun observeConnectivity(context: Context): Flow<Boolean> = callbackFlow {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    trySend(checkCurrentOnline(context))
    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        trySend(true)
      }

      override fun onLost(network: Network) {
        trySend(checkCurrentOnline(context))
      }
    }
    var registered = false
    if (cm != null) {
      try {
        val req = NetworkRequest.Builder()
          .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
          .build()
        cm.registerNetworkCallback(req, callback)
        registered = true
      } catch (e: Exception) {
        Log.w(TAG, "Failed to register network callback", e)
      }
    }
    awaitClose {
      if (registered && cm != null) {
        try {
          cm.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
        }
      }
    }
  }

  private companion object {
    const val TAG = "HisabViewModel"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
