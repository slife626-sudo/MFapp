package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryItem
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.MarketList
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.PeriodFilter
import com.example.data.model.Receivable
import com.example.data.model.ReceivablePayment
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.LocalStrings
import com.example.ui.components.AccountWalletsStrip
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CollectionRowCard
import com.example.ui.components.IncomeVsExpenseChart
import com.example.ui.components.MarketQuickEntryCard
import com.example.ui.components.NetCashFlowAreaChart
import com.example.ui.components.PayableRowCard
import com.example.ui.components.PeriodFilterStrip
import com.example.ui.components.ReceivableRowCard
import com.example.ui.components.RepaymentRowCard
import com.example.ui.components.SensitiveAmountText
import com.example.ui.components.TransferRowCard
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.DashboardUiData
import com.example.viewmodel.HisabAnalytics
import java.util.Calendar

const val FILTER_INCOME = "__income__"
const val FILTER_TRANSFER = "__transfer__"
const val FILTER_PAYABLE = "__payable__"
const val FILTER_RECEIVABLE = "__receivable__"

private sealed interface DashFeedEntry {
  val key: String
  val dateMillis: Long

  data class ExpenseItem(val expense: Expense) : DashFeedEntry {
    override val key: String get() = "exp_${expense.id}"
    override val dateMillis: Long get() = expense.dateMillis
  }

  data class IncomeItem(val income: Income) : DashFeedEntry {
    override val key: String get() = "inc_${income.id}"
    override val dateMillis: Long get() = income.dateMillis
  }

  data class TransferItem(val transfer: AccountTransfer) : DashFeedEntry {
    override val key: String get() = "tr_${transfer.id}"
    override val dateMillis: Long get() = transfer.dateMillis
  }

  data class PayableItem(val payable: Payable, val paidAmount: Double) : DashFeedEntry {
    override val key: String get() = "pay_${payable.id}"
    override val dateMillis: Long get() = payable.dateMillis
  }

  data class PaymentItem(val payment: PayablePayment) : DashFeedEntry {
    override val key: String get() = "pmt_${payment.id}"
    override val dateMillis: Long get() = payment.dateMillis
  }

  data class ReceivableItem(val receivable: Receivable, val collectedAmount: Double) : DashFeedEntry {
    override val key: String get() = "rec_${receivable.id}"
    override val dateMillis: Long get() = receivable.dateMillis
  }

  data class CollectionItem(val payment: ReceivablePayment) : DashFeedEntry {
    override val key: String get() = "col_${payment.id}"
    override val dateMillis: Long get() = payment.dateMillis
  }
}

@Composable
fun DashboardScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  isOnline: Boolean,
  selectedMonthKey: String,
  selectedCategoryFilter: String?,
  searchQuery: String,
  onShiftMonth: (Int) -> Unit,
  onSelectCategoryFilter: (String?) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onEditExpense: (Expense) -> Unit,
  onDeleteExpense: (Expense) -> Unit,
  onNavigateToBudgets: () -> Unit,
  onAddBalance: () -> Unit = {},
  onEditIncome: (Income) -> Unit = {},
  onDeleteIncome: (Income) -> Unit = {},
  onOpenTransfer: () -> Unit = {},
  onOpenBorrow: () -> Unit = {},
  onNavigateToWallets: () -> Unit = {},
  onEditTransfer: (AccountTransfer) -> Unit = {},
  onDeleteTransfer: (AccountTransfer) -> Unit = {},
  onEditPayable: (Payable) -> Unit = {},
  onDeletePayable: (Payable) -> Unit = {},
  onRepayPayable: (Payable) -> Unit = {},
  onDeleteRepayment: (PayablePayment) -> Unit = {},
  marketLists: List<MarketList> = emptyList(),
  currentUserId: String = "",
  onOpenMarket: () -> Unit = {},
  onOpenLend: () -> Unit = {},
  onEditReceivable: (Receivable) -> Unit = {},
  onDeleteReceivable: (Receivable) -> Unit = {},
  onCollectReceivable: (Receivable) -> Unit = {},
  onDeleteCollection: (ReceivablePayment) -> Unit = {},
  selectedPeriod: PeriodFilter = PeriodFilter.THIS_MONTH,
  onSelectPeriod: (PeriodFilter) -> Unit = {},
  onNavigateToTransactions: () -> Unit = {},
  onOpenAddAccount: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  val langCode = settings.language.code

  var expensePendingDelete by remember { mutableStateOf<Expense?>(null) }
  var incomePendingDelete by remember { mutableStateOf<Income?>(null) }
  var transferPendingDelete by remember { mutableStateOf<AccountTransfer?>(null) }
  var payablePendingDelete by remember { mutableStateOf<Payable?>(null) }
  var repaymentPendingDelete by remember { mutableStateOf<PayablePayment?>(null) }
  var receivablePendingDelete by remember { mutableStateOf<Receivable?>(null) }
  var collectionPendingDelete by remember { mutableStateOf<ReceivablePayment?>(null) }
  var donutTab by remember { mutableIntStateOf(0) } // 0 = Expense, 1 = Income

  when (uiState) {
    UiState.Loading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }

    is UiState.Error -> Box(
      modifier = modifier.fillMaxSize().padding(24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(uiState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
    }

    is UiState.Success -> {
      val data = uiState.data
      val categories = remember(strings, data.customCategories) {
        CategoryCatalog.mergeCategories(strings, data.customCategories)
      }
      val allCategories = remember(strings, data.customCategories) {
        CategoryCatalog.mergeCategories(strings, data.customCategories, includeIncome = true)
      }
      val catMap = remember(allCategories) { allCategories.associateBy { it.id } }

      val periodAnalytics = remember(data, selectedPeriod, selectedMonthKey, settings.language) {
        HisabAnalytics.computePeriodAnalytics(data, selectedPeriod, selectedMonthKey, settings.language)
      }
      val accountSummaries = remember(data, strings, settings.currencyCode) {
        data.resolveAccountSummaries(strings, settings.currencyCode)
      }
      val balances = remember(data) { data.accountBalances() }
      val totalUnpaidPayables = remember(data) { data.totalUnpaidPayables() }
      val totalUnpaidReceivables = remember(data) { data.totalUnpaidReceivables() }
      val hasAnyWalletActivity = remember(data) {
        data.incomes.isNotEmpty() ||
          data.payables.isNotEmpty() ||
          data.receivables.isNotEmpty() ||
          data.transfers.isNotEmpty() ||
          data.customAccounts.isNotEmpty()
      }

      val startMs = periodAnalytics.startMillis
      val endMs = periodAnalytics.endMillis
      val isSpecialFilter = selectedCategoryFilter == FILTER_INCOME ||
        selectedCategoryFilter == FILTER_TRANSFER ||
        selectedCategoryFilter == FILTER_PAYABLE ||
        selectedCategoryFilter == FILTER_RECEIVABLE

      val filteredEntries = remember(
        data,
        startMs,
        endMs,
        selectedCategoryFilter,
        searchQuery,
      ) {
        val q = searchQuery.trim().lowercase()
        val expList = if (isSpecialFilter) emptyList() else {
          data.expenses.filter { exp ->
            exp.dateMillis in startMs..endMs &&
              (selectedCategoryFilter == null || exp.categoryId == selectedCategoryFilter) &&
              (q.isEmpty() || exp.title.lowercase().contains(q) || exp.note.lowercase().contains(q))
          }.map { DashFeedEntry.ExpenseItem(it) }
        }
        val incList = if (selectedCategoryFilter != null && selectedCategoryFilter != FILTER_INCOME) emptyList() else {
          data.incomes.filter { inc ->
            inc.dateMillis in startMs..endMs &&
              (q.isEmpty() || inc.title.lowercase().contains(q) || inc.note.lowercase().contains(q))
          }.map { DashFeedEntry.IncomeItem(it) }
        }
        val trList = if (selectedCategoryFilter != null && selectedCategoryFilter != FILTER_TRANSFER) emptyList() else {
          data.transfers.filter { tr ->
            tr.dateMillis in startMs..endMs &&
              (q.isEmpty() || tr.fromMethod.lowercase().contains(q) || tr.toMethod.lowercase().contains(q) || tr.note.lowercase().contains(q))
          }.map { DashFeedEntry.TransferItem(it) }
        }
        val payList = if (selectedCategoryFilter != null && selectedCategoryFilter != FILTER_PAYABLE) emptyList() else {
          data.payables.filter { pay ->
            pay.dateMillis in startMs..endMs &&
              (q.isEmpty() || pay.personName.lowercase().contains(q) || pay.note.lowercase().contains(q))
          }.map { DashFeedEntry.PayableItem(it, data.paidAmountForPayable(it)) }
        }
        val pmtList = if (selectedCategoryFilter != null && selectedCategoryFilter != FILTER_PAYABLE) emptyList() else {
          data.payablePayments.filter { pmt ->
            pmt.dateMillis in startMs..endMs &&
              (q.isEmpty() || pmt.personName.lowercase().contains(q) || pmt.note.lowercase().contains(q))
          }.map { DashFeedEntry.PaymentItem(it) }
        }
        val recList = if (selectedCategoryFilter != null && selectedCategoryFilter != FILTER_RECEIVABLE) emptyList() else {
          data.receivables.filter { rec ->
            rec.dateMillis in startMs..endMs &&
              (q.isEmpty() || rec.personName.lowercase().contains(q) || rec.note.lowercase().contains(q))
          }.map { DashFeedEntry.ReceivableItem(it, data.collectedAmountForReceivable(it)) }
        }
        val colList = if (selectedCategoryFilter != null && selectedCategoryFilter != FILTER_RECEIVABLE) emptyList() else {
          data.receivablePayments.filter { col ->
            col.dateMillis in startMs..endMs &&
              (q.isEmpty() || col.personName.lowercase().contains(q) || col.note.lowercase().contains(q))
          }.map { DashFeedEntry.CollectionItem(it) }
        }
        (expList + incList + trList + payList + pmtList + recList + colList)
          .sortedByDescending { it.dateMillis }
      }

      val groupedByDay = remember(filteredEntries) {
        filteredEntries.groupBy { DateFormatUtils.formatDayKey(it.dateMillis) }
      }

      LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
              )
              Text(
                text = strings.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            SyncStatusPill(
              cloudSyncEnabled = settings.cloudSyncEnabled,
              isOnline = isOnline,
              hasPendingWrites = data.hasPendingWrites,
            )
          }
        }

        item {
          PeriodFilterStrip(
            selectedPeriod = selectedPeriod,
            selectedMonthKey = selectedMonthKey,
            langCode = langCode,
            onSelectPeriod = onSelectPeriod,
            onShiftMonth = onShiftMonth,
          )
        }

        // Hero Net Balance & Budget Card
        item {
          HeroSpendingCard(
            monthKey = periodAnalytics.monthKey,
            totalSpent = periodAnalytics.totalSpent,
            totalIncome = periodAnalytics.totalIncome,
            hasAnyIncome = hasAnyWalletActivity,
            totalWalletBalance = balances.total,
            totalUnpaidPayables = totalUnpaidPayables,
            totalUnpaidReceivables = totalUnpaidReceivables,
            budgetLimit = periodAnalytics.budgetLimit,
            currencySymbol = currencySymbol,
            langCode = langCode,
            blurBalances = settings.blurBalances,
            onClickBudget = onNavigateToBudgets,
            onClickPayables = onNavigateToWallets,
            onAddBalance = onAddBalance,
          )
        }

        // Today's Activity + Period Savings Rate KPI Row
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.weight(1.25f),
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = "${strings.todaysActivityTitle} (${periodAnalytics.todayCount})",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  SensitiveAmountText(
                    text = "+${CurrencyFormat.format(periodAnalytics.todayIncome, currencySymbol)}",
                    blurBalances = settings.blurBalances,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SemanticSafeLight,
                  )
                  SensitiveAmountText(
                    text = "-${CurrencyFormat.format(periodAnalytics.todaySpent, currencySymbol)}",
                    blurBalances = settings.blurBalances,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SemanticDangerLight,
                  )
                }
              }
            }
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.weight(0.75f),
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = strings.savingsRateLabel,
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                val pct = (periodAnalytics.savingsRate * 100f).toInt()
                Text(
                  text = "$pct%",
                  fontFamily = HisabMonoFontFamily,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = if (pct >= 0) SemanticSafeLight else SemanticDangerLight,
                )
              }
            }
          }
        }

        // Account Wallets Strip & Quick Actions
        item {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.accountsLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              TextButton(onClick = onOpenAddAccount, modifier = Modifier.testTag("dash_add_account_btn")) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(strings.addAccountButton)
              }
            }
            AccountWalletsStrip(
              balances = balances,
              totalUnpaidPayables = totalUnpaidPayables,
              currencySymbol = currencySymbol,
              onClickAccounts = onNavigateToWallets,
              onAddBalance = onAddBalance,
              onOpenTransfer = onOpenTransfer,
              onOpenBorrow = onOpenBorrow,
              onOpenLend = onOpenLend,
              accountSummaries = accountSummaries,
              blurBalances = settings.blurBalances,
            )
          }
        }

        // Market Quick Entry Card
        item {
          val activeMarketCount = remember(marketLists) { marketLists.count { !it.completed } }
          val sharedFromOthersCount = remember(marketLists, currentUserId) {
            marketLists.count { !it.completed && it.ownerUid != currentUserId }
          }
          MarketQuickEntryCard(
            activeCount = activeMarketCount,
            sharedReceivedCount = sharedFromOthersCount,
            onClick = onOpenMarket,
          )
        }

        // Net Cash Flow & Income vs Expense Charts
        if (periodAnalytics.netCashFlowSeries.isNotEmpty()) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = strings.netCashFlowTitle,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
                NetCashFlowAreaChart(
                  series = periodAnalytics.netCashFlowSeries,
                  currencySymbol = currencySymbol,
                  surplusLabel = strings.surplusLabel,
                  deficitLabel = strings.deficitLabel,
                  blurBalances = settings.blurBalances,
                )
              }
            }
          }

          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = strings.incomeVsExpenseTitle,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
                IncomeVsExpenseChart(
                  series = periodAnalytics.incomeVsExpenseMonthly,
                  currencySymbol = currencySymbol,
                  incomeLabel = strings.totalIncomeLabel,
                  expenseLabel = strings.totalSpent,
                  barLabel = strings.barChartLabel,
                  lineLabel = strings.lineChartLabel,
                  blurBalances = settings.blurBalances,
                )
              }
            }
          }
        }

        // Category Donut Chart (Expense / Income toggle) + Top Categories
        val activeBreakdowns = if (donutTab == 0) periodAnalytics.expenseBreakdowns else periodAnalytics.incomeBreakdowns
        val activeTotal = if (donutTab == 0) periodAnalytics.totalSpent else periodAnalytics.totalIncome
        if (periodAnalytics.expenseBreakdowns.isNotEmpty() || periodAnalytics.incomeBreakdowns.isNotEmpty()) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = strings.topCategoriesTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                  )
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                      selected = donutTab == 0,
                      onClick = { donutTab = 0 },
                      label = { Text(strings.expenseCategoryType) },
                    )
                    FilterChip(
                      selected = donutTab == 1,
                      onClick = { donutTab = 1 },
                      label = { Text(strings.incomeCategoryType) },
                    )
                  }
                }
                CategoryDonutChart(
                  breakdowns = activeBreakdowns,
                  totalSpent = activeTotal,
                  currencySymbol = currencySymbol,
                  totalLabel = if (donutTab == 0) strings.totalSpent else strings.totalIncomeLabel,
                )
              }
            }
          }
        }

        // Recent Transactions Header + Search + Filters
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = strings.recentTransactionsTitle,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            TextButton(
              onClick = onNavigateToTransactions,
              modifier = Modifier.testTag("view_all_transactions_btn"),
            ) {
              Text(strings.viewAllButton)
            }
          }
        }

        item {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(strings.searchExpenses) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.searchExpenses) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("search_expenses_input"),
          )
        }

        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FilterChip(
              selected = selectedCategoryFilter == null,
              onClick = { onSelectCategoryFilter(null) },
              label = { Text(strings.allCategories) },
            )
            FilterChip(
              selected = selectedCategoryFilter == FILTER_INCOME,
              onClick = {
                onSelectCategoryFilter(if (selectedCategoryFilter == FILTER_INCOME) null else FILTER_INCOME)
              },
              leadingIcon = {
                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = strings.balanceFilterChip, tint = SemanticSafeLight, modifier = Modifier.size(16.dp))
              },
              label = { Text(strings.balanceFilterChip) },
              modifier = Modifier.testTag("filter_income_chip"),
            )
            FilterChip(
              selected = selectedCategoryFilter == FILTER_TRANSFER,
              onClick = {
                onSelectCategoryFilter(if (selectedCategoryFilter == FILTER_TRANSFER) null else FILTER_TRANSFER)
              },
              leadingIcon = {
                Icon(Icons.Rounded.SwapHoriz, contentDescription = strings.transferFilterChip, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              },
              label = { Text(strings.transferFilterChip) },
              modifier = Modifier.testTag("filter_transfer_chip"),
            )
            FilterChip(
              selected = selectedCategoryFilter == FILTER_PAYABLE,
              onClick = {
                onSelectCategoryFilter(if (selectedCategoryFilter == FILTER_PAYABLE) null else FILTER_PAYABLE)
              },
              leadingIcon = {
                Icon(Icons.Rounded.Handshake, contentDescription = strings.payableFilterChip, tint = SemanticWarningLight, modifier = Modifier.size(16.dp))
              },
              label = { Text(strings.payableFilterChip) },
              modifier = Modifier.testTag("filter_payable_chip"),
            )
            FilterChip(
              selected = selectedCategoryFilter == FILTER_RECEIVABLE,
              onClick = {
                onSelectCategoryFilter(if (selectedCategoryFilter == FILTER_RECEIVABLE) null else FILTER_RECEIVABLE)
              },
              leadingIcon = {
                Icon(Icons.Rounded.NorthEast, contentDescription = strings.receivableFilterChip, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              },
              label = { Text(strings.receivableFilterChip) },
              modifier = Modifier.testTag("filter_receivable_chip"),
            )
            categories.forEach { cat ->
              FilterChip(
                selected = selectedCategoryFilter == cat.id,
                onClick = {
                  onSelectCategoryFilter(if (selectedCategoryFilter == cat.id) null else cat.id)
                },
                leadingIcon = {
                  Icon(
                    imageVector = CategoryCatalog.iconFor(cat.iconKey),
                    contentDescription = cat.name,
                    tint = Color(cat.colorHex),
                    modifier = Modifier.size(16.dp),
                  )
                },
                label = { Text(cat.name) },
              )
            }
          }
        }

        if (filteredEntries.isEmpty()) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
              shape = RoundedCornerShape(24.dp),
              modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Box(
                  modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp),
                  )
                }
                Spacer(Modifier.height(12.dp))
                Text(strings.emptyExpensesTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                  text = strings.emptyExpensesBody,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        } else {
          groupedByDay.forEach { (_, dayEntries) ->
            val firstMillis = dayEntries.first().dateMillis
            val daySpentSum = dayEntries.filterIsInstance<DashFeedEntry.ExpenseItem>().sumOf { it.expense.amount }
            val dayIncomeSum = dayEntries.filterIsInstance<DashFeedEntry.IncomeItem>().sumOf { it.income.amount }
            item(key = "header_${DateFormatUtils.formatDayKey(firstMillis)}") {
              Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = DateFormatUtils.formatDayHeader(firstMillis, strings.today, strings.yesterday, langCode),
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  if (dayIncomeSum > 0.0) {
                    SensitiveAmountText(
                      text = "+${CurrencyFormat.format(dayIncomeSum, currencySymbol)}",
                      blurBalances = settings.blurBalances,
                      style = MaterialTheme.typography.labelLarge,
                      color = SemanticSafeLight,
                    )
                  }
                  if (daySpentSum > 0.0) {
                    SensitiveAmountText(
                      text = "-${CurrencyFormat.format(daySpentSum, currencySymbol)}",
                      blurBalances = settings.blurBalances,
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }
            }

            items(dayEntries, key = { it.key }) { entry ->
              when (entry) {
                is DashFeedEntry.ExpenseItem -> {
                  val expense = entry.expense
                  val cat = catMap[expense.categoryId] ?: CategoryItem(
                    id = expense.categoryId,
                    name = expense.categoryId,
                    iconKey = "other",
                    colorHex = 0xFF0891B2,
                  )
                  DashExpenseRowCard(
                    expense = expense,
                    category = cat,
                    accountLabel = data.accountLabel(expense.accountId, expense.paymentMethod, strings),
                    currencySymbol = currencySymbol,
                    showNotes = settings.showNotesInList,
                    blurBalances = settings.blurBalances,
                    onClick = { onEditExpense(expense) },
                    onDelete = { expensePendingDelete = expense },
                  )
                }

                is DashFeedEntry.IncomeItem -> {
                  val inc = entry.income
                  val incCat = inc.categoryId?.let { catMap[it] }
                  DashIncomeRowCard(
                    income = inc,
                    categoryName = incCat?.name,
                    accountLabel = data.accountLabel(inc.accountId, inc.paymentMethod, strings),
                    currencySymbol = currencySymbol,
                    showNotes = settings.showNotesInList,
                    blurBalances = settings.blurBalances,
                    onClick = { onEditIncome(inc) },
                    onDelete = { incomePendingDelete = inc },
                  )
                }

                is DashFeedEntry.TransferItem -> {
                  val tr = entry.transfer
                  TransferRowCard(
                    transfer = tr,
                    currencySymbol = currencySymbol,
                    fromAccountLabel = data.accountLabel(tr.fromAccountId, tr.fromMethod, strings),
                    toAccountLabel = data.accountLabel(tr.toAccountId, tr.toMethod, strings),
                    showNotes = settings.showNotesInList,
                    blurBalances = settings.blurBalances,
                    onClick = { onEditTransfer(tr) },
                    onDelete = { transferPendingDelete = tr },
                  )
                }

                is DashFeedEntry.PayableItem -> {
                  val pay = entry.payable
                  PayableRowCard(
                    payable = pay,
                    paidAmount = entry.paidAmount,
                    currencySymbol = currencySymbol,
                    accountLabel = data.accountLabel(pay.accountId, pay.paymentMethod, strings),
                    showNotes = settings.showNotesInList,
                    blurBalances = settings.blurBalances,
                    langCode = langCode,
                    onClick = { onEditPayable(pay) },
                    onRepay = { onRepayPayable(pay) },
                    onDelete = { payablePendingDelete = pay },
                  )
                }

                is DashFeedEntry.PaymentItem -> {
                  val pmt = entry.payment
                  RepaymentRowCard(
                    payment = pmt,
                    currencySymbol = currencySymbol,
                    accountLabel = data.accountLabel(pmt.accountId, pmt.paymentMethod, strings),
                    showNotes = settings.showNotesInList,
                    blurBalances = settings.blurBalances,
                    onDelete = { repaymentPendingDelete = pmt },
                  )
                }

                is DashFeedEntry.ReceivableItem -> {
                  val rec = entry.receivable
                  ReceivableRowCard(
                    receivable = rec,
                    collectedAmount = entry.collectedAmount,
                    currencySymbol = currencySymbol,
                    accountLabel = data.accountLabel(rec.accountId, rec.paymentMethod, strings),
                    showNotes = settings.showNotesInList,
                    blurBalances = settings.blurBalances,
                    langCode = langCode,
                    onClick = { onEditReceivable(rec) },
                    onCollect = { onCollectReceivable(rec) },
                    onDelete = { receivablePendingDelete = rec },
                  )
                }

                is DashFeedEntry.CollectionItem -> {
                  val col = entry.payment
                  CollectionRowCard(
                    payment = col,
                    currencySymbol = currencySymbol,
                    accountLabel = data.accountLabel(col.accountId, col.paymentMethod, strings),
                    showNotes = settings.showNotesInList,
                    blurBalances = settings.blurBalances,
                    onDelete = { collectionPendingDelete = col },
                  )
                }
              }
            }
          }

          if (filteredEntries.size > 15) {
            item {
              OutlinedButton(
                onClick = onNavigateToTransactions,
                modifier = Modifier.fillMaxWidth().height(48.dp),
              ) {
                Text(strings.viewAllButton)
              }
            }
          }
        }
      }
    }
  }

  expensePendingDelete?.let { pending ->
    DashDeleteConfirmDialog(
      title = strings.confirmDeleteTitle,
      message = "${pending.title} — ${strings.confirmDeleteMessage}",
      onConfirm = { onDeleteExpense(pending); expensePendingDelete = null },
      onDismiss = { expensePendingDelete = null },
    )
  }
  incomePendingDelete?.let { pending ->
    DashDeleteConfirmDialog(
      title = strings.confirmDeleteBalanceTitle,
      message = "${pending.title} — ${strings.confirmDeleteBalanceMessage}",
      onConfirm = { onDeleteIncome(pending); incomePendingDelete = null },
      onDismiss = { incomePendingDelete = null },
    )
  }
  transferPendingDelete?.let { pending ->
    DashDeleteConfirmDialog(
      title = strings.confirmDeleteTransferTitle,
      message = strings.confirmDeleteTransferMessage,
      onConfirm = { onDeleteTransfer(pending); transferPendingDelete = null },
      onDismiss = { transferPendingDelete = null },
    )
  }
  payablePendingDelete?.let { pending ->
    DashDeleteConfirmDialog(
      title = strings.confirmDeletePayableTitle,
      message = "${pending.personName} — ${strings.confirmDeletePayableMessage}",
      onConfirm = { onDeletePayable(pending); payablePendingDelete = null },
      onDismiss = { payablePendingDelete = null },
    )
  }
  repaymentPendingDelete?.let { pending ->
    DashDeleteConfirmDialog(
      title = strings.confirmDeleteRepaymentTitle,
      message = "${pending.personName} — ${strings.confirmDeleteRepaymentMessage}",
      onConfirm = { onDeleteRepayment(pending); repaymentPendingDelete = null },
      onDismiss = { repaymentPendingDelete = null },
    )
  }
  receivablePendingDelete?.let { pending ->
    DashDeleteConfirmDialog(
      title = strings.confirmDeleteReceivableTitle,
      message = "${pending.personName} — ${strings.confirmDeleteReceivableMessage}",
      onConfirm = { onDeleteReceivable(pending); receivablePendingDelete = null },
      onDismiss = { receivablePendingDelete = null },
    )
  }
  collectionPendingDelete?.let { pending ->
    DashDeleteConfirmDialog(
      title = strings.confirmDeleteCollectionTitle,
      message = "${pending.personName} — ${strings.confirmDeleteCollectionMessage}",
      onConfirm = { onDeleteCollection(pending); collectionPendingDelete = null },
      onDismiss = { collectionPendingDelete = null },
    )
  }
}

@Composable
private fun DashDeleteConfirmDialog(
  title: String,
  message: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val strings = LocalStrings.current
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = { Text(message) },
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
      ) { Text(strings.delete) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    },
  )
}

@Composable
private fun SyncStatusPill(
  cloudSyncEnabled: Boolean,
  isOnline: Boolean,
  hasPendingWrites: Boolean,
) {
  val strings = LocalStrings.current
  val (icon, label, containerColor) = when {
    !cloudSyncEnabled -> Triple(Icons.Rounded.CloudOff, strings.offlineModeActive, MaterialTheme.colorScheme.surfaceContainerHigh)
    !isOnline -> Triple(Icons.Rounded.CloudOff, strings.offlineCached, MaterialTheme.colorScheme.tertiaryContainer)
    hasPendingWrites -> Triple(Icons.Rounded.Sync, strings.syncingPending, MaterialTheme.colorScheme.secondaryContainer)
    else -> Triple(Icons.Rounded.CloudDone, strings.onlineSynced, MaterialTheme.colorScheme.primaryContainer)
  }
  Surface(shape = RoundedCornerShape(50), color = containerColor) {
    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(15.dp))
      Spacer(Modifier.width(5.dp))
      Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
  }
}

@Composable
private fun HeroSpendingCard(
  monthKey: String,
  totalSpent: Double,
  totalIncome: Double,
  hasAnyIncome: Boolean,
  totalWalletBalance: Double,
  totalUnpaidPayables: Double,
  totalUnpaidReceivables: Double,
  budgetLimit: Double,
  currencySymbol: String,
  langCode: String,
  blurBalances: Boolean,
  onClickBudget: () -> Unit,
  onClickPayables: () -> Unit,
  onAddBalance: () -> Unit,
) {
  val strings = LocalStrings.current
  val primary = MaterialTheme.colorScheme.primary
  val secondary = MaterialTheme.colorScheme.tertiary
  val ratio = if (budgetLimit > 0.0) (totalSpent / budgetLimit).toFloat().coerceIn(0f, 1f) else 0f
  val daysInMonth = DateFormatUtils.daysInMonth(monthKey)
  val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
  val remainingDays = if (monthKey == DateFormatUtils.currentMonthKey()) {
    (daysInMonth - todayDay + 1).coerceAtLeast(1)
  } else daysInMonth
  val remainingBudget = budgetLimit - totalSpent
  val showBalanceMode = hasAnyIncome

  Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(colors = listOf(primary, primary.copy(alpha = 0.85f), secondary.copy(alpha = 0.78f)))
        )
        .padding(20.dp),
    ) {
      Column {
        Text(
          text = if (showBalanceMode) strings.remainingBalance else strings.thisMonthSpend,
          style = MaterialTheme.typography.labelMedium,
          color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(4.dp))
        val headlineText = if (showBalanceMode) {
          CurrencyFormat.formatSigned(totalWalletBalance, currencySymbol)
        } else {
          CurrencyFormat.format(totalSpent, currencySymbol)
        }
        SensitiveAmountText(
          text = headlineText,
          blurBalances = blurBalances,
          style = MaterialTheme.typography.displayLarge,
          color = if (showBalanceMode && totalWalletBalance < 0.0) Color(0xFFFFD6D6) else Color.White,
          fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Surface(
            color = Color.White.copy(alpha = 0.18f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .clickable(onClick = onAddBalance)
              .testTag("add_balance_button"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.addedBalance,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.82f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                SensitiveAmountText(
                  text = "+${CurrencyFormat.format(totalIncome, currencySymbol)}",
                  blurBalances = blurBalances,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFBDF2DF),
                )
              }
              Icon(Icons.Rounded.Add, contentDescription = strings.addBalance, tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }

          Surface(
            color = Color.White.copy(alpha = 0.14f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f),
          ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
              Text(
                text = strings.totalSpent,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              SensitiveAmountText(
                text = "-${CurrencyFormat.format(totalSpent, currencySymbol)}",
                blurBalances = blurBalances,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
              )
            }
          }
        }

        if (totalUnpaidPayables > 0.0 || totalUnpaidReceivables > 0.0) {
          Spacer(Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (totalUnpaidPayables > 0.0) {
              Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClickPayables),
              ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                  Text(strings.totalPayablesLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                  SensitiveAmountText(
                    text = CurrencyFormat.format(totalUnpaidPayables, currencySymbol),
                    blurBalances = blurBalances,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFE0B2),
                  )
                }
              }
            }
            if (totalUnpaidReceivables > 0.0) {
              Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClickPayables),
              ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                  Text(strings.totalReceivablesLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                  SensitiveAmountText(
                    text = CurrencyFormat.format(totalUnpaidReceivables, currencySymbol),
                    blurBalances = blurBalances,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBDF2DF),
                  )
                }
              }
            }
          }
        }

        Spacer(Modifier.height(12.dp))
        if (budgetLimit > 0.0) {
          val progressColor = when {
            totalSpent >= budgetLimit -> SemanticDangerLight
            totalSpent >= budgetLimit * 0.8 -> SemanticWarningLight
            else -> Color(0xFFBDF2DF)
          }
          LinearProgressIndicator(
            progress = { ratio },
            color = progressColor,
            trackColor = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
          )
          Spacer(Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
              text = if (remainingBudget >= 0) {
                "${strings.budgetRemaining}: ${if (blurBalances) "••••" else CurrencyFormat.format(remainingBudget, currencySymbol)}"
              } else {
                "${strings.budgetExceededBy}: ${if (blurBalances) "••••" else CurrencyFormat.format(-remainingBudget, currencySymbol)}"
              },
              style = MaterialTheme.typography.labelMedium,
              color = Color.White,
            )
            if (remainingBudget > 0) {
              Text(
                text = "${strings.dailyAllowance}: ${CurrencyFormat.format(remainingBudget / remainingDays, currencySymbol)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
              )
            }
          }
        } else {
          Surface(
            color = Color.White.copy(alpha = 0.16f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClickBudget),
          ) {
            Text(
              text = "${strings.setBudgetHint} (${DateFormatUtils.formatMonthLabel(monthKey, langCode)}) →",
              style = MaterialTheme.typography.labelLarge,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DashIncomeRowCard(
  income: Income,
  categoryName: String?,
  accountLabel: String,
  currencySymbol: String,
  showNotes: Boolean,
  blurBalances: Boolean,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("income_item_${income.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(SemanticSafeLight.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(Icons.Rounded.ArrowDownward, contentDescription = strings.entryTypeIncome, tint = SemanticSafeLight, modifier = Modifier.size(22.dp))
      }
      Spacer(Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(income.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
          text = buildString {
            append(categoryName ?: strings.entryTypeIncome)
            append(" • ")
            append(accountLabel)
            if (showNotes && income.note.isNotBlank()) {
              append(" • ")
              append(income.note)
            }
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(Modifier.width(8.dp))
      SensitiveAmountText(
        text = "+${CurrencyFormat.format(income.amount, currencySymbol)}",
        blurBalances = blurBalances,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = SemanticSafeLight,
      )
      IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_income_${income.id}")) {
        Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
private fun DashExpenseRowCard(
  expense: Expense,
  category: CategoryItem,
  accountLabel: String,
  currencySymbol: String,
  showNotes: Boolean,
  blurBalances: Boolean,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("expense_item_${expense.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(category.colorHex).copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = CategoryCatalog.iconFor(category.iconKey),
          contentDescription = category.name,
          tint = Color(category.colorHex),
          modifier = Modifier.size(22.dp),
        )
      }
      Spacer(Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(expense.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
          text = buildString {
            append(category.name)
            append(" • ")
            append(accountLabel)
            if (showNotes && expense.note.isNotBlank()) {
              append(" • ")
              append(expense.note)
            }
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(Modifier.width(8.dp))
      SensitiveAmountText(
        text = "-${CurrencyFormat.format(expense.amount, currencySymbol)}",
        blurBalances = blurBalances,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_expense_${expense.id}")) {
        Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}
