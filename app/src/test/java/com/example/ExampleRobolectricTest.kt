package com.example

import android.content.Context
import android.graphics.Typeface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.MarketList
import com.example.data.model.MarketListItem
import com.example.data.model.Payable
import com.example.data.model.Receivable
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.i18n.stringsFor
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.HisabTheme
import com.example.viewmodel.AccountBalances
import com.example.viewmodel.CategoryBreakdown
import com.example.viewmodel.DashboardUiData
import com.example.viewmodel.MonthlySummary
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
  fun `auth and primary screens compose in bengali without crashing`() {
    val settings = UserSettings(language = AppLanguage.BENGALI)
    val strings = stringsFor(settings.language)
    val builtInCats = CategoryCatalog.builtInCategories(strings)
    val sampleData = DashboardUiData(
      expenses = listOf(
        Expense(
          id = "e1",
          userId = "u1",
          title = "চাল ডাল",
          amount = 450.0,
          categoryId = "groceries",
          dateMillis = System.currentTimeMillis(),
          monthKey = "2026-11",
        )
      ),
      incomes = listOf(
        Income(
          id = "i1",
          userId = "u1",
          title = "বেতন",
          amount = 20000.0,
          dateMillis = System.currentTimeMillis(),
          monthKey = "2026-11",
        )
      ),
      transfers = listOf(AccountTransfer(id = "t1", amount = 500.0, monthKey = "2026-11")),
      payables = listOf(Payable(id = "p1", personName = "করিম", amount = 1000.0, monthKey = "2026-11")),
      receivables = listOf(Receivable(id = "r1", personName = "রহিম", amount = 500.0, monthKey = "2026-11")),
      customCategories = emptyList(),
      budgets = emptyList(),
      hasPendingWrites = false,
      isFromCache = false,
    )
    val summary = MonthlySummary(
      monthKey = "2026-11",
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
            selectedMonthKey = "2026-11",
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
            selectedMonthKey = "2026-11",
            onShiftMonth = {},
            computeSummary = { summary },
          )
          BudgetsScreen(
            uiState = UiState.Success(sampleData),
            settings = settings,
            selectedMonthKey = "2026-11",
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
}
