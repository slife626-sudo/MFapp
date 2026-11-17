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
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryItem
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.CustomCategory
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentMethod
import com.example.data.model.UiState
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.prefs.UserSettings
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.SyncSnapshot
import com.example.i18n.AppLanguage
import com.example.i18n.stringsFor
import com.example.notifications.ReminderScheduler
import com.example.ui.theme.AppThemePalette
import com.example.ui.theme.ThemeMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
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

data class DashboardUiData(
  val expenses: List<Expense>,
  val incomes: List<Income>,
  val customCategories: List<CustomCategory>,
  val budgets: List<MonthlyBudget>,
  val hasPendingWrites: Boolean,
  val isFromCache: Boolean,
)

class HisabViewModel(
  private val repository: ExpenseRepository,
  private val prefsRepo: UserPreferencesRepository,
  private val appContext: Context,
  private val currentUserId: String,
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

  private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val messages = _messages.asSharedFlow()

  private var pinInitialized = false

  val dashboardState: StateFlow<UiState<DashboardUiData>> = combine(
    repository.observeExpenses(currentUserId),
    repository.observeIncomes(currentUserId),
    repository.observeCustomCategories(currentUserId),
    repository.observeBudgets(currentUserId),
  ) { expSnap: SyncSnapshot<Expense>, incSnap: SyncSnapshot<Income>, cats: List<CustomCategory>, budgets: List<MonthlyBudget> ->
    UiState.Success(
      DashboardUiData(
        expenses = expSnap.items,
        incomes = incSnap.items,
        customCategories = cats,
        budgets = budgets,
        hasPendingWrites = expSnap.hasPendingWrites || incSnap.hasPendingWrites,
        isFromCache = expSnap.isFromCache && incSnap.isFromCache,
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
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val active = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(active) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  private fun observeConnectivity(context: Context): Flow<Boolean> = callbackFlow {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    trySend(checkCurrentOnline(context))
    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        trySend(true)
      }

      override fun onLost(network: Network) {
        trySend(checkCurrentOnline(context))
      }
    }
    val req = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()
    cm.registerNetworkCallback(req, callback)
    awaitClose { cm.unregisterNetworkCallback(callback) }
  }

  private companion object {
    const val TAG = "HisabViewModel"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
