package com.example

import android.content.Context
import android.graphics.Typeface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet as OutlinedWallet
import androidx.compose.material.icons.outlined.GridView as OutlinedGridView
import androidx.compose.material.icons.outlined.LocalOffer as OutlinedLocalOffer
import androidx.compose.material.icons.outlined.Payments as OutlinedPayments
import androidx.compose.material.icons.outlined.SwapHoriz as OutlinedSwapHoriz
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupSerializer
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryFlowType
import com.example.data.model.CustomAccount
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
import com.example.data.model.PeriodFilter
import com.example.data.model.Receivable
import com.example.data.model.ReceivablePayment
import com.example.data.model.TrashItem
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.i18n.stringsFor
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.screens.TrialBalanceScreen
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.HisabTheme
import com.example.viewmodel.AccountBalances
import com.example.viewmodel.CategoryBreakdown
import com.example.viewmodel.DashboardUiData
import com.example.viewmodel.HisabAnalytics
import com.example.viewmodel.MonthlySummary
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hisab", appName)
  }

  @Test
  fun `fonts and drawables load cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sans: Typeface? = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans)
    val bengali: Typeface? = ResourcesCompat.getFont(context, R.font.noto_sans_bengali)
    val mono: Typeface? = ResourcesCompat.getFont(context, R.font.jetbrains_mono)
    assertNotNull(sans)
    assertNotNull(bengali)
    assertNotNull(mono)
    assertNotNull(ResourcesCompat.getDrawable(context.resources, R.drawable.img_auth_hero, context.theme))
    assertNotNull(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_launcher_foreground, context.theme))
  }

  @Test
  fun `all language string tables load without class format errors`() {
    AppLanguage.entries.forEach { lang ->
      val s = stringsFor(lang)
      assertNotNull(s.appName)
      assertNotNull(s.splitByCategoryOption)
      assertNotNull(s.itemCategoryLabel)
      assertNotNull(s.navTrialBalance)
      assertNotNull(s.balancedBadge)
    }
  }

  @Test
  fun `firebase initializes with configured database id`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance()
    assertNotNull(app)
    assertNotNull(FirebaseAuth.getInstance())
    val dbId = context.getString(R.string.firestore_database_id)
    assertNotNull(FirebaseFirestore.getInstance(dbId))
  }

  @Test
  fun `main activity launches without crashing`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    assertNotNull(activity)
    composeRule.waitForIdle()
  }

  @Test
  fun `trial balance, account ledger, and custom account routing stay mathematically balanced`() {
    val strings = stringsFor(AppLanguage.BENGALI)
    val monthKey = DateFormatUtils.currentMonthKey()
    val (startMillis, endMillis) = DateFormatUtils.periodRangeMillis(PeriodFilter.THIS_MONTH, monthKey)
    val txTime = ((startMillis + endMillis) / 2L).coerceAtMost(System.currentTimeMillis())

    val bkash = CustomAccount(
      id = "acct_bkash",
      name = "Bkash",
      accountType = PaymentMethod.MOBILE.wireValue,
      iconKey = "mobile_pay",
      colorHex = 0xFFDB2777,
      openingBalance = 5000.0,
    )
    val ibbl = CustomAccount(
      id = "acct_ibbl",
      name = "IBBL",
      accountType = PaymentMethod.BANK.wireValue,
      iconKey = "bank",
      colorHex = 0xFF2563EB,
      openingBalance = 10000.0,
    )
    val data = DashboardUiData(
      expenses = listOf(
        Expense(
          id = "e1",
          title = "দুপুরের খাবার",
          amount = 1500.0,
          categoryId = "food",
          paymentMethod = PaymentMethod.MOBILE.wireValue,
          accountId = "acct_bkash",
          dateMillis = txTime,
          monthKey = monthKey,
        )
      ),
      incomes = listOf(
        Income(
          id = "i1",
          title = "বেতন",
          amount = 20000.0,
          categoryId = "inc_salary",
          paymentMethod = PaymentMethod.BANK.wireValue,
          accountId = "acct_ibbl",
          dateMillis = txTime,
          monthKey = monthKey,
        )
      ),
      transfers = listOf(
        AccountTransfer(
          id = "t1",
          fromMethod = PaymentMethod.BANK.wireValue,
          toMethod = PaymentMethod.CASH.wireValue,
          fromAccountId = "acct_ibbl",
          toAccountId = null,
          amount = 3000.0,
          dateMillis = txTime,
          monthKey = monthKey,
        )
      ),
      payables = listOf(
        Payable(
          id = "p1",
          personName = "করিম",
          amount = 2000.0,
          paidAmount = 500.0,
          paymentMethod = PaymentMethod.CASH.wireValue,
          dateMillis = txTime,
          dueDateMillis = txTime + 86_400_000L,
          monthKey = monthKey,
        )
      ),
      payablePayments = listOf(
        PayablePayment(
          id = "pmt1",
          payableId = "p1",
          personName = "করিম",
          amount = 500.0,
          paymentMethod = PaymentMethod.MOBILE.wireValue,
          accountId = "acct_bkash",
          dateMillis = txTime,
          monthKey = monthKey,
        )
      ),
      receivables = listOf(
        Receivable(
          id = "r1",
          personName = "রহিম",
          amount = 1000.0,
          collectedAmount = 400.0,
          paymentMethod = PaymentMethod.CASH.wireValue,
          dateMillis = txTime,
          monthKey = monthKey,
        )
      ),
      receivablePayments = listOf(
        ReceivablePayment(
          id = "rpmt1",
          receivableId = "r1",
          personName = "রহিম",
          amount = 400.0,
          paymentMethod = PaymentMethod.CASH.wireValue,
          dateMillis = txTime,
          monthKey = monthKey,
        )
      ),
      customCategories = listOf(
        CustomCategory(id = "cc1", name = "ফ্রিল্যান্স", categoryType = CategoryFlowType.INCOME.wireValue)
      ),
      budgets = listOf(MonthlyBudget(monthKey = monthKey, totalLimit = 10000.0)),
      hasPendingWrites = false,
      isFromCache = false,
      customAccounts = listOf(bkash, ibbl),
    )

    val summaries = data.resolveAccountSummaries(strings, "BDT")
    val bkashSummary = summaries.first { it.accountId == "acct_bkash" }
    val ibblSummary = summaries.first { it.accountId == "acct_ibbl" }
    val cashSummary = summaries.first { it.accountId == DashboardUiData.DEFAULT_ACCOUNT_PREFIX + "CASH" }

    // Bkash: 5000 - 1500 (exp) - 500 (repay) = 3000
    assertEquals(3000.0, bkashSummary.currentBalance, 0.01)
    // IBBL: 10000 + 20000 (inc) - 3000 (transfer out) = 27000
    assertEquals(27000.0, ibblSummary.currentBalance, 0.01)
    // Cash: +3000 (tr in) +2000 (borrow) -1000 (lend) +400 (collect) = 4400
    assertEquals(4400.0, cashSummary.currentBalance, 0.01)

    val ledger = HisabAnalytics.computeAccountLedger(data, "acct_bkash", startMillis, endMillis, strings)
    assertEquals(5000.0, ledger.openingBalance, 0.01)
    assertEquals(3000.0, ledger.closingBalance, 0.01)

    val period = HisabAnalytics.computePeriodAnalytics(data, PeriodFilter.THIS_MONTH, monthKey, AppLanguage.BENGALI)
    assertEquals(20000.0, period.totalIncome, 0.01)
    assertEquals(1500.0, period.totalSpent, 0.01)

    val trialBalance = HisabAnalytics.computeTrialBalance(data, startMillis, endMillis, strings, "BDT")
    assertTrue("Trial balance must always be balanced", trialBalance.isBalanced)
    assertEquals(trialBalance.totalDebit, trialBalance.totalCredit, 0.01)
  }

  @Test
  fun `backup serializer v5 round trips custom accounts and extended fields`() {
    val json = BackupSerializer.toJsonBackup(
      expenses = listOf(
        Expense(
          id = "e1",
          title = "বাজার",
          amount = 500.0,
          categoryId = "groceries",
          paymentMethod = PaymentMethod.MOBILE.wireValue,
          accountId = "acct_1",
          dateMillis = 1_760_000_000_000L,
          monthKey = "2026-10",
        )
      ),
      customCategories = listOf(
        CustomCategory(
          id = "c1",
          name = "বোনাস",
          iconKey = "salary",
          colorHex = 0xFF0E7C5B,
          categoryType = CategoryFlowType.INCOME.wireValue,
        )
      ),
      budgets = emptyList(),
      incomes = listOf(
        Income(
          id = "i1",
          title = "বেতন",
          amount = 25000.0,
          categoryId = "inc_salary",
          paymentMethod = PaymentMethod.BANK.wireValue,
          accountId = "acct_2",
          dateMillis = 1_760_000_000_000L,
          monthKey = "2026-10",
        )
      ),
      transfers = listOf(
        AccountTransfer(
          id = "t1",
          fromMethod = PaymentMethod.BANK.wireValue,
          toMethod = PaymentMethod.BANK.wireValue,
          fromAccountId = "acct_2",
          toAccountId = "acct_3",
          amount = 1000.0,
          dateMillis = 1_760_000_000_000L,
          monthKey = "2026-10",
        )
      ),
      payables = listOf(
        Payable(
          id = "p1",
          personName = "করিম",
          amount = 1200.0,
          paymentMethod = PaymentMethod.CASH.wireValue,
          dueDateMillis = 1_765_000_000_000L,
          dateMillis = 1_760_000_000_000L,
          monthKey = "2026-10",
        )
      ),
      customAccounts = listOf(
        CustomAccount(
          id = "acct_1",
          name = "Bkash",
          accountType = PaymentMethod.MOBILE.wireValue,
          openingBalance = 2500.0,
        )
      ),
    )
    val parsed = BackupSerializer.parseJsonBackup(json)
    assertEquals(1, parsed.customAccounts.size)
    assertEquals("Bkash", parsed.customAccounts.first().name)
    assertEquals(2500.0, parsed.customAccounts.first().openingBalance, 0.01)
    assertEquals("acct_1", parsed.expenses.first().accountId)
    assertEquals("inc_salary", parsed.incomes.first().categoryId)
    assertEquals("acct_2", parsed.transfers.first().fromAccountId)
    assertEquals("acct_3", parsed.transfers.first().toAccountId)
    assertEquals(1_765_000_000_000L, parsed.payables.first().dueDateMillis)
    assertEquals(CategoryFlowType.INCOME.wireValue, parsed.customCategories.first().categoryType)
  }

  @Test
  fun `auth and all primary and accounting screens compose in bengali without crashing`() {
    val settings = UserSettings(language = AppLanguage.BENGALI)
    val strings = stringsFor(settings.language)
    val builtInCats = CategoryCatalog.builtInCategories(strings)
    val now = System.currentTimeMillis()
    val monthKey = DateFormatUtils.currentMonthKey()
    val sampleData = DashboardUiData(
      expenses = listOf(
        Expense(
          id = "e1",
          userId = "u1",
          title = "চাল ডাল",
          amount = 450.0,
          categoryId = "groceries",
          dateMillis = now,
          monthKey = monthKey,
        )
      ),
      incomes = listOf(
        Income(
          id = "i1",
          userId = "u1",
          title = "বেতন",
          amount = 20000.0,
          categoryId = "inc_salary",
          dateMillis = now,
          monthKey = monthKey,
        )
      ),
      transfers = listOf(AccountTransfer(id = "t1", amount = 500.0, dateMillis = now, monthKey = monthKey)),
      payables = listOf(Payable(id = "p1", personName = "করিম", amount = 1000.0, dateMillis = now, monthKey = monthKey)),
      receivables = listOf(Receivable(id = "r1", personName = "রহিম", amount = 500.0, dateMillis = now, monthKey = monthKey)),
      customCategories = listOf(CustomCategory(id = "cc1", name = "বোনাস", categoryType = "INCOME")),
      budgets = emptyList(),
      hasPendingWrites = false,
      isFromCache = false,
      customAccounts = listOf(CustomAccount(id = "a1", name = "IBBL", accountType = "BANK", openingBalance = 1000.0)),
      trashItems = listOf(
        TrashItem(id = "tr1", entityType = "EXPENSE", sourceId = "e0", title = "নাস্তা", amount = 80.0, deletedAtMillis = now)
      ),
    )
    val summary = MonthlySummary(
      monthKey = monthKey,
      totalSpent = 450.0,
      totalIncome = 20000.0,
      netBalance = 19550.0,
      prevMonthSpent = 300.0,
      transactionCount = 1,
      dailyAverage = 25.0,
      topCategory = CategoryBreakdown(builtInCats.first(), 450.0, 1.0f, 1, 1000.0),
      budgetLimit = 5000.0,
      categoryBreakdowns = listOf(CategoryBreakdown(builtInCats.first(), 450.0, 1.0f, 1, 1000.0)),
      dailyTotals = mapOf(1 to 450.0),
      sixMonthTotals = listOf("Jun" to 0.0, "Jul" to 0.0, "Aug" to 0.0, "Sep" to 0.0, "Oct" to 300.0, "Nov" to 450.0),
    )

    composeRule.setContent {
      CompositionLocalProvider(LocalStrings provides strings) {
        HisabTheme {
          AuthScreen(
            selectedLanguage = settings.language,
            onLanguageSelected = {},
            onAuthSuccess = {},
          )
          DashboardScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            isOnline = true,
            selectedMonthKey = monthKey,
            selectedCategoryFilter = null,
            searchQuery = "",
            onShiftMonth = {},
            onSelectCategoryFilter = {},
            onSearchQueryChange = {},
            onEditExpense = {},
            onDeleteExpense = {},
            onNavigateToBudgets = {},
            marketLists = listOf(MarketList(id = "m1", title = "আজকের বাজার")),
            currentUserId = "u1",
          )
          TransactionsScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            selectedPeriod = PeriodFilter.THIS_MONTH,
            selectedMonthKey = monthKey,
            onSelectPeriod = {},
            onShiftMonth = {},
            onEditExpense = {},
            onEditIncome = {},
            onEditTransfer = {},
            onEditPayable = {},
            onEditReceivable = {},
            onDeleteSingle = { _, _ -> },
            onBulkSoftDelete = {},
          )
          CategoriesScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            selectedPeriod = PeriodFilter.THIS_MONTH,
            selectedMonthKey = monthKey,
            onSelectPeriod = {},
            onShiftMonth = {},
            onAddCustomCategory = { _, _, _, _ -> },
            onUpdateCustomCategory = { _, _, _, _, _ -> },
            onDeleteCustomCategory = {},
            onBulkDeleteCustomCategories = {},
          )
          LedgerScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            selectedPeriod = PeriodFilter.THIS_MONTH,
            selectedMonthKey = monthKey,
            onSelectPeriod = {},
            onShiftMonth = {},
            onExportLedgerCsv = {},
          )
          TrialBalanceScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            selectedPeriod = PeriodFilter.THIS_MONTH,
            selectedMonthKey = monthKey,
            onSelectPeriod = {},
            onShiftMonth = {},
            onExportTrialBalanceCsv = {},
          )
          TrashScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            onRestoreItem = {},
            onDeleteForever = {},
            onEmptyTrash = {},
          )
          WalletsScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            onAddBalance = {},
            onOpenTransfer = {},
            onOpenBorrow = {},
            onEditTransfer = {},
            onDeleteTransfer = {},
            onEditPayable = {},
            onDeletePayable = {},
            onRepayPayable = {},
            onDeleteRepayment = {},
          )
          MarketScreen(
            marketLists = listOf(MarketList(id = "m1", ownerUid = "u1", title = "আজকের বাজার")),
            selectedListId = "m1",
            selectedItems = listOf(
              MarketListItem(id = "it1", listId = "m1", name = "চাল", estimatedPrice = 200.0, categoryId = "groceries"),
              MarketListItem(id = "it2", listId = "m1", name = "প্যারাসিটামল", estimatedPrice = 50.0, categoryId = "health"),
            ),
            categories = builtInCats,
            currentUserId = "u1",
            accountBalances = AccountBalances(cash = 1000.0),
            settings = settings,
            onBackToDashboard = {},
            onSelectList = {},
            onCreateList = { _, _, _ -> },
            onUpdateListAndShare = { _, _, _, _ -> },
            onToggleListCompleted = { _, _ -> },
            onDeleteList = {},
            onAddItem = { _, _, _, _, _ -> },
            onUpdateItem = { _, _, _, _, _, _, _ -> },
            onToggleItemBought = { _, _ -> },
            onDeleteItem = { _, _ -> },
            onLogAsExpense = { _, _, _, _, _ -> },
          )
          ReportsScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            selectedMonthKey = monthKey,
            onShiftMonth = {},
            computeSummary = { summary },
          )
          BudgetsScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            selectedMonthKey = monthKey,
            onSaveBudget = { _, _, _ -> },
            onToggleDailyReminder = { _, _, _ -> },
            onToggleBudgetAlerts = {},
          )
          SettingsScreen(
            userDisplayName = "User",
            userEmail = "user@example.com",
            settings = settings,
            customCategories = emptyList(),
            isBusy = false,
            driveFiles = emptyList(),
            onSignOut = {},
            onToggleCloudSync = {},
            onBackupToDrive = {},
            onFetchDriveBackups = { _, _ -> },
            onRestoreDriveFile = { _, _ -> },
            onBuildCsvString = { "" },
            onBuildJsonString = { "{}" },
            onImportJsonString = {},
            onEnablePin = {},
            onDisablePin = {},
            onToggleLockOnBackground = {},
            onSelectThemeMode = {},
            onSelectThemePalette = {},
            onSelectLanguage = {},
            onSelectCurrency = {},
            onAddCustomCategory = { _, _, _ -> },
            onDeleteCustomCategory = {},
            onNotifyMessage = {},
          )
        }
      }
    }
    composeRule.waitForIdle()
  }

  @Test
  fun `bengali payable strings use dena without English Payable`() {
    val bn = stringsFor(AppLanguage.BENGALI)
    val allPayableTexts = listOf(
      bn.addBorrow,
      bn.editBorrow,
      bn.borrowAutoPayableHint,
      bn.payableFilterChip,
      bn.walletsScreenTitle,
      bn.totalPayablesLabel,
      bn.unpaidPayablesTitle,
      bn.settledPayablesTitle,
      bn.noPayablesEmpty,
      bn.repayPayableTitle,
      bn.confirmDeletePayableTitle,
      bn.entryTypeBorrow,
      bn.entryTypeRepayment,
      bn.payablesTab,
    )
    allPayableTexts.forEach { text ->
      assertTrue("Unexpected 'Payable' in '$text'", !text.contains("Payable", ignoreCase = true))
    }
    listOf(
      bn.addBorrow,
      bn.borrowAutoPayableHint,
      bn.totalPayablesLabel,
      bn.unpaidPayablesTitle,
      bn.entryTypeBorrow,
      bn.entryTypeRepayment,
      bn.payablesTab,
    ).forEach { text ->
      assertTrue("Expected দেনা in '$text'", text.contains("দেনা"))
    }
  }

  @Test
  fun `ribbon bottom bar highlights whichever menu item is selected`() {
    val bn = stringsFor(AppLanguage.BENGALI)
    val items = listOf(
      com.example.ui.components.RibbonNavItem(
        route = ROUTE_DASHBOARD,
        label = bn.navDashboard,
        selectedIcon = Icons.Rounded.GridView,
        unselectedIcon = Icons.Outlined.OutlinedGridView,
      ),
      com.example.ui.components.RibbonNavItem(
        route = ROUTE_TRANSACTIONS,
        label = bn.navTransactions,
        selectedIcon = Icons.Rounded.SwapHoriz,
        unselectedIcon = Icons.Outlined.OutlinedSwapHoriz,
      ),
      com.example.ui.components.RibbonNavItem(
        route = ROUTE_BUDGETS,
        label = bn.navBudgets,
        selectedIcon = Icons.Rounded.Payments,
        unselectedIcon = Icons.Outlined.OutlinedPayments,
      ),
      com.example.ui.components.RibbonNavItem(
        route = ROUTE_WALLETS,
        label = bn.navAccounts,
        selectedIcon = Icons.Rounded.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.OutlinedWallet,
      ),
      com.example.ui.components.RibbonNavItem(
        route = ROUTE_CATEGORIES,
        label = bn.navCategories,
        selectedIcon = Icons.Rounded.LocalOffer,
        unselectedIcon = Icons.Outlined.OutlinedLocalOffer,
      ),
    )

    val selectedRouteState = mutableStateOf(ROUTE_DASHBOARD)
    composeRule.setContent {
      CompositionLocalProvider(LocalStrings provides bn) {
        HisabTheme {
          com.example.ui.components.HisabRibbonBottomBar(
            items = items,
            currentRoute = selectedRouteState.value,
            onNavigate = { selectedRouteState.value = it },
          )
        }
      }
    }

    composeRule.waitForIdle()
    composeRule.onNode(hasTestTag("nav_$ROUTE_DASHBOARD") and isSelected()).assertExists()

    // Switch through every tab and verify selection state updates
    listOf(ROUTE_TRANSACTIONS, ROUTE_BUDGETS, ROUTE_WALLETS, ROUTE_CATEGORIES, ROUTE_DASHBOARD).forEach { target ->
      composeRule.onNode(hasTestTag("nav_$target")).performClick()
      composeRule.waitForIdle()
      assertEquals(target, selectedRouteState.value)
      composeRule.onNode(hasTestTag("nav_$target") and isSelected()).assertExists()
    }
  }
}
