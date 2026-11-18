package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CurrencyOption
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.Payable
import com.example.data.model.Receivable
import com.example.data.model.UiState
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.prefs.UserSettings
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MarketListRepository
import com.example.i18n.LocalStrings
import com.example.i18n.stringsFor
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.components.AddEditExpenseSheet
import com.example.ui.components.CollectReceivableDialog
import com.example.ui.components.EntrySheetMode
import com.example.ui.components.PinLockOverlay
import com.example.ui.components.RepayPayableDialog
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.HisabTheme
import com.example.viewmodel.AccountBalances
import com.example.viewmodel.HisabViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_WALLETS = "wallets"
const val ROUTE_MARKET = "market"
const val ROUTE_REPORTS = "reports"
const val ROUTE_BUDGETS = "budgets"
const val ROUTE_SETTINGS = "settings"

private data class NavDestination(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth ->
    trySend(auth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val context = LocalContext.current
      val prefsRepo = remember(context) { UserPreferencesRepository(context.applicationContext) }
      val settings by prefsRepo.settingsFlow.collectAsStateWithLifecycle(initialValue = UserSettings())
      val strings = remember(settings.language) { stringsFor(settings.language) }
      val scope = rememberCoroutineScope()

      CompositionLocalProvider(LocalStrings provides strings) {
        HisabTheme(
          themePalette = settings.themePalette,
          themeMode = settings.themeMode,
        ) {
          AppNavigation(
            settings = settings,
            onSelectLanguage = { lang -> scope.launch { prefsRepo.setLanguage(lang) } },
          )
        }
      }
    }
  }
}

@Composable
fun AppNavigation(
  settings: UserSettings,
  onSelectLanguage: (com.example.i18n.AppLanguage) -> Unit,
  auth: FirebaseAuth = Firebase.auth,
) {
  val currentUser by auth.authStateFlow()
    .collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(
      selectedLanguage = settings.language,
      onLanguageSelected = onSelectLanguage,
      onAuthSuccess = {},
    )
  } else {
    key(user.uid) {
      AuthenticatedHisabApp(
        currentUser = user,
        settings = settings,
      )
    }
  }
}

@Composable
private fun AuthenticatedHisabApp(
  currentUser: FirebaseUser,
  settings: UserSettings,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val strings = LocalStrings.current

  val viewModel: HisabViewModel = viewModel(
    key = currentUser.uid,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val databaseId = app.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        val repository = ExpenseRepository(db)
        val marketRepository = MarketListRepository(db)
        val prefsRepo = UserPreferencesRepository(app.applicationContext)
        HisabViewModel(
          repository = repository,
          marketRepository = marketRepository,
          prefsRepo = prefsRepo,
          appContext = app.applicationContext,
          currentUserId = currentUser.uid,
          currentUserEmail = currentUser.email.orEmpty(),
          currentUserName = currentUser.displayName.orEmpty(),
        )
      }
    },
  )

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_STOP) {
        viewModel.onAppBackgrounded()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()
  val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
  val selectedMonthKey by viewModel.selectedMonthKey.collectAsStateWithLifecycle()
  val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
  val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
  val driveFiles by viewModel.driveFiles.collectAsStateWithLifecycle()
  val marketLists by viewModel.marketLists.collectAsStateWithLifecycle()
  val selectedMarketListId by viewModel.selectedMarketListId.collectAsStateWithLifecycle()
  val selectedMarketItems by viewModel.selectedMarketItems.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(viewModel) {
    viewModel.messages.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: ROUTE_DASHBOARD

  var showExpenseSheet by remember { mutableStateOf(false) }
  var editingExpense by remember { mutableStateOf<Expense?>(null) }
  var editingIncome by remember { mutableStateOf<Income?>(null) }
  var editingTransfer by remember { mutableStateOf<AccountTransfer?>(null) }
  var editingPayable by remember { mutableStateOf<Payable?>(null) }
  var editingReceivable by remember { mutableStateOf<Receivable?>(null) }
  var sheetInitialMode by remember { mutableStateOf(EntrySheetMode.EXPENSE) }
  var repayingPayable by remember { mutableStateOf<Payable?>(null) }
  var collectingReceivable by remember { mutableStateOf<Receivable?>(null) }

  fun openNewSheet(mode: EntrySheetMode) {
    editingExpense = null
    editingIncome = null
    editingTransfer = null
    editingPayable = null
    editingReceivable = null
    sheetInitialMode = mode
    showExpenseSheet = true
  }

  fun closeSheet() {
    showExpenseSheet = false
    editingExpense = null
    editingIncome = null
    editingTransfer = null
    editingPayable = null
    editingReceivable = null
    sheetInitialMode = EntrySheetMode.EXPENSE
  }

  val destinations = remember(strings) {
    listOf(
      NavDestination(ROUTE_DASHBOARD, strings.navExpenses, Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong),
      NavDestination(ROUTE_WALLETS, strings.navWallets, Icons.Rounded.AccountBalance, Icons.Outlined.AccountBalance),
      NavDestination(ROUTE_REPORTS, strings.navReports, Icons.Rounded.BarChart, Icons.Outlined.BarChart),
      NavDestination(ROUTE_BUDGETS, strings.navBudgets, Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
      NavDestination(ROUTE_SETTINGS, strings.navSettings, Icons.Rounded.Settings, Icons.Outlined.Settings),
    )
  }

  fun navigateTo(route: String) {
    navController.navigate(route) {
      popUpTo(ROUTE_DASHBOARD) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  val successData = (uiState as? UiState.Success)?.data
  val customCategories = successData?.customCategories.orEmpty()
  val categories = remember(strings, customCategories) {
    CategoryCatalog.mergeCategories(strings, customCategories)
  }
  val accountBalances = remember(successData) {
    successData?.accountBalances() ?: AccountBalances()
  }
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol

  Box(modifier = Modifier.fillMaxSize()) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val useNavRail = maxWidth >= 600.dp

      Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
          if (!useNavRail) {
            NavigationBar {
              destinations.forEach { dest ->
                val selected = currentRoute == dest.route
                NavigationBarItem(
                  selected = selected,
                  onClick = { navigateTo(dest.route) },
                  icon = {
                    Icon(
                      imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                      contentDescription = dest.label,
                    )
                  },
                  label = {
                    Text(
                      text = dest.label,
                      maxLines = 1,
                      softWrap = false,
                      overflow = TextOverflow.Ellipsis,
                    )
                  },
                  modifier = Modifier.testTag("nav_${dest.route}"),
                )
              }
            }
          }
        },
        floatingActionButton = {
          if (currentRoute == ROUTE_DASHBOARD || currentRoute == ROUTE_REPORTS || currentRoute == ROUTE_WALLETS) {
            FloatingActionButton(
              onClick = { openNewSheet(EntrySheetMode.EXPENSE) },
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.testTag("fab_add_expense"),
            ) {
              Icon(Icons.Rounded.Add, contentDescription = strings.addExpense)
            }
          }
        },
      ) { innerPadding ->
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        ) {
          if (useNavRail) {
            NavigationRail(modifier = Modifier.fillMaxHeight()) {
              destinations.forEach { dest ->
                val selected = currentRoute == dest.route
                NavigationRailItem(
                  selected = selected,
                  onClick = { navigateTo(dest.route) },
                  icon = {
                    Icon(
                      imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                      contentDescription = dest.label,
                    )
                  },
                  label = { Text(dest.label) },
                  modifier = Modifier.testTag("rail_${dest.route}"),
                )
              }
              NavigationRailItem(
                selected = currentRoute == ROUTE_MARKET,
                onClick = { navigateTo(ROUTE_MARKET) },
                icon = {
                  Icon(
                    imageVector = if (currentRoute == ROUTE_MARKET) Icons.Rounded.ShoppingBag else Icons.Outlined.ShoppingBag,
                    contentDescription = strings.marketListTitle,
                  )
                },
                label = { Text(strings.marketListTitle) },
                modifier = Modifier.testTag("rail_$ROUTE_MARKET"),
              )
            }
          }

          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
          ) {
            NavHost(
              navController = navController,
              startDestination = ROUTE_DASHBOARD,
              modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
            ) {
              composable(ROUTE_DASHBOARD) {
                DashboardScreen(
                  uiState = uiState,
                  settings = settings,
                  isOnline = isOnline,
                  selectedMonthKey = selectedMonthKey,
                  selectedCategoryFilter = selectedCategoryFilter,
                  searchQuery = searchQuery,
                  onShiftMonth = viewModel::shiftMonth,
                  onSelectCategoryFilter = viewModel::setCategoryFilter,
                  onSearchQueryChange = viewModel::setSearchQuery,
                  onEditExpense = { exp ->
                    closeSheet()
                    editingExpense = exp
                    showExpenseSheet = true
                  },
                  onDeleteExpense = { exp -> viewModel.deleteExpense(exp.id) },
                  onNavigateToBudgets = { navigateTo(ROUTE_BUDGETS) },
                  onAddBalance = { openNewSheet(EntrySheetMode.INCOME) },
                  onEditIncome = { inc ->
                    closeSheet()
                    editingIncome = inc
                    showExpenseSheet = true
                  },
                  onDeleteIncome = { inc -> viewModel.deleteIncome(inc.id) },
                  onOpenTransfer = { openNewSheet(EntrySheetMode.TRANSFER) },
                  onOpenBorrow = { openNewSheet(EntrySheetMode.BORROW) },
                  onNavigateToWallets = { navigateTo(ROUTE_WALLETS) },
                  onEditTransfer = { tr ->
                    closeSheet()
                    editingTransfer = tr
                    showExpenseSheet = true
                  },
                  onDeleteTransfer = { tr -> viewModel.deleteTransfer(tr.id) },
                  onEditPayable = { pay ->
                    closeSheet()
                    editingPayable = pay
                    showExpenseSheet = true
                  },
                  onDeletePayable = { pay -> viewModel.deletePayable(pay.id) },
                  onRepayPayable = { pay -> repayingPayable = pay },
                  onDeleteRepayment = { pmt -> viewModel.deletePayablePayment(pmt) },
                  marketLists = marketLists,
                  currentUserId = currentUser.uid,
                  onOpenMarket = { navigateTo(ROUTE_MARKET) },
                  onOpenLend = { openNewSheet(EntrySheetMode.LEND) },
                  onEditReceivable = { rec ->
                    closeSheet()
                    editingReceivable = rec
                    showExpenseSheet = true
                  },
                  onDeleteReceivable = { rec -> viewModel.deleteReceivable(rec.id) },
                  onCollectReceivable = { rec -> collectingReceivable = rec },
                  onDeleteCollection = { col -> viewModel.deleteReceivablePayment(col) },
                )
              }

              composable(ROUTE_WALLETS) {
                WalletsScreen(
                  uiState = uiState,
                  settings = settings,
                  onAddBalance = { openNewSheet(EntrySheetMode.INCOME) },
                  onOpenTransfer = { openNewSheet(EntrySheetMode.TRANSFER) },
                  onOpenBorrow = { openNewSheet(EntrySheetMode.BORROW) },
                  onEditTransfer = { tr ->
                    closeSheet()
                    editingTransfer = tr
                    showExpenseSheet = true
                  },
                  onDeleteTransfer = { tr -> viewModel.deleteTransfer(tr.id) },
                  onEditPayable = { pay ->
                    closeSheet()
                    editingPayable = pay
                    showExpenseSheet = true
                  },
                  onDeletePayable = { pay -> viewModel.deletePayable(pay.id) },
                  onRepayPayable = { pay -> repayingPayable = pay },
                  onDeleteRepayment = { pmt -> viewModel.deletePayablePayment(pmt) },
                  onOpenLend = { openNewSheet(EntrySheetMode.LEND) },
                  onEditReceivable = { rec ->
                    closeSheet()
                    editingReceivable = rec
                    showExpenseSheet = true
                  },
                  onDeleteReceivable = { rec -> viewModel.deleteReceivable(rec.id) },
                  onCollectReceivable = { rec -> collectingReceivable = rec },
                  onDeleteCollection = { col -> viewModel.deleteReceivablePayment(col) },
                )
              }

              composable(ROUTE_MARKET) {
                MarketScreen(
                  marketLists = marketLists,
                  selectedListId = selectedMarketListId,
                  selectedItems = selectedMarketItems,
                  categories = categories,
                  currentUserId = currentUser.uid,
                  accountBalances = accountBalances,
                  settings = settings,
                  onBackToDashboard = { navController.popBackStack() },
                  onSelectList = viewModel::selectMarketList,
                  onCreateList = viewModel::createMarketList,
                  onUpdateListAndShare = viewModel::updateMarketListAndShare,
                  onToggleListCompleted = viewModel::toggleMarketListCompleted,
                  onDeleteList = viewModel::deleteMarketList,
                  onAddItem = { list, name, qty, price, categoryId ->
                    viewModel.addMarketItem(list, name, qty, price, categoryId)
                  },
                  onUpdateItem = { listId, item, name, qty, price, categoryId, bought ->
                    viewModel.updateMarketItem(listId, item, name, qty, price, categoryId, bought)
                  },
                  onToggleItemBought = viewModel::toggleMarketItemBought,
                  onDeleteItem = viewModel::deleteMarketItem,
                  onLogAsExpense = { list, amount, method, categoryId, splitByCategory ->
                    viewModel.logMarketListAsExpense(
                      list = list,
                      totalAmount = amount,
                      paymentMethod = method,
                      categoryId = categoryId,
                      splitByCategory = splitByCategory,
                    )
                  },
                )
              }

              composable(ROUTE_REPORTS) {
                ReportsScreen(
                  uiState = uiState,
                  settings = settings,
                  selectedMonthKey = selectedMonthKey,
                  onShiftMonth = viewModel::shiftMonth,
                  computeSummary = { data ->
                    viewModel.computeMonthlySummary(data, selectedMonthKey, settings.language)
                  },
                )
              }

              composable(ROUTE_BUDGETS) {
                BudgetsScreen(
                  uiState = uiState,
                  settings = settings,
                  selectedMonthKey = selectedMonthKey,
                  onSaveBudget = viewModel::saveBudget,
                  onToggleDailyReminder = viewModel::setDailyReminder,
                  onToggleBudgetAlerts = viewModel::setBudgetAlertsEnabled,
                )
              }

              composable(ROUTE_SETTINGS) {
                SettingsScreen(
                  userDisplayName = currentUser.displayName.orEmpty(),
                  userEmail = currentUser.email.orEmpty(),
                  settings = settings,
                  customCategories = customCategories,
                  isBusy = isBusy,
                  driveFiles = driveFiles,
                  onSignOut = {
                    signOutUser(credentialManager, scope)
                  },
                  onToggleCloudSync = viewModel::setCloudSyncEnabled,
                  onBackupToDrive = viewModel::uploadBackupToDrive,
                  onFetchDriveBackups = viewModel::fetchDriveBackups,
                  onRestoreDriveFile = viewModel::restoreFromDriveFile,
                  onBuildCsvString = viewModel::buildCsvExportString,
                  onBuildJsonString = viewModel::buildJsonBackupString,
                  onImportJsonString = viewModel::importJsonBackup,
                  onEnablePin = viewModel::enablePin,
                  onDisablePin = viewModel::disablePin,
                  onToggleLockOnBackground = viewModel::setLockOnBackground,
                  onSelectThemeMode = viewModel::setThemeMode,
                  onSelectThemePalette = viewModel::setThemePalette,
                  onSelectLanguage = viewModel::setLanguage,
                  onSelectCurrency = viewModel::setCurrencyCode,
                  onAddCustomCategory = viewModel::addCustomCategory,
                  onDeleteCustomCategory = viewModel::deleteCustomCategory,
                  onNotifyMessage = viewModel::notifyMessage,
                )
              }
            }
          }
        }
      }
    }

    if (showExpenseSheet) {
      AddEditExpenseSheet(
        initialExpense = editingExpense,
        initialIncome = editingIncome,
        initialTransfer = editingTransfer,
        initialPayable = editingPayable,
        initialReceivable = editingReceivable,
        initialMode = sheetInitialMode,
        accountBalances = accountBalances,
        categories = categories,
        currencySymbol = currencySymbol,
        langCode = settings.language.code,
        onDismiss = { closeSheet() },
        onSaveExpense = { title, amount, categoryId, method, note, dateMillis ->
          val existing = editingExpense
          if (existing == null) {
            viewModel.addExpense(title, amount, categoryId, method, note, dateMillis)
          } else {
            viewModel.updateExpense(existing.id, title, amount, categoryId, method, note, dateMillis)
          }
          closeSheet()
        },
        onSaveIncome = { title, amount, method, note, dateMillis ->
          val existing = editingIncome
          if (existing == null) {
            viewModel.addIncome(title, amount, method, note, dateMillis)
          } else {
            viewModel.updateIncome(existing.id, title, amount, method, note, dateMillis)
          }
          closeSheet()
        },
        onSaveTransfer = { fromMethod, toMethod, amount, note, dateMillis ->
          val existing = editingTransfer
          if (existing == null) {
            viewModel.addTransfer(fromMethod, toMethod, amount, note, dateMillis)
          } else {
            viewModel.updateTransfer(existing.id, fromMethod, toMethod, amount, note, dateMillis)
          }
          closeSheet()
        },
        onSavePayable = { personName, amount, method, note, dateMillis ->
          val existing = editingPayable
          if (existing == null) {
            viewModel.addPayable(personName, amount, method, note, dateMillis)
          } else {
            viewModel.updatePayable(existing, personName, amount, method, note, dateMillis)
          }
          closeSheet()
        },
        onSaveReceivable = { personName, amount, method, note, dateMillis ->
          val existing = editingReceivable
          if (existing == null) {
            viewModel.addReceivable(personName, amount, method, note, dateMillis)
          } else {
            viewModel.updateReceivable(existing, personName, amount, method, note, dateMillis)
          }
          closeSheet()
        },
        onCreateCustomCategory = viewModel::addCustomCategory,
      )
    }

    val activePayableToRepay = repayingPayable
    if (activePayableToRepay != null) {
      val paidAlready = successData?.paidAmountForPayable(activePayableToRepay) ?: activePayableToRepay.paidAmount
      RepayPayableDialog(
        payable = activePayableToRepay,
        paidAlready = paidAlready,
        accountBalances = accountBalances,
        currencySymbol = currencySymbol,
        langCode = settings.language.code,
        onDismiss = { repayingPayable = null },
        onConfirm = { amount, method, note, dateMillis ->
          viewModel.repayPayable(activePayableToRepay, amount, method, note, dateMillis)
          repayingPayable = null
        },
      )
    }

    val activeReceivableToCollect = collectingReceivable
    if (activeReceivableToCollect != null) {
      val collectedAlready = successData?.collectedAmountForReceivable(activeReceivableToCollect)
        ?: activeReceivableToCollect.collectedAmount
      CollectReceivableDialog(
        receivable = activeReceivableToCollect,
        collectedAlready = collectedAlready,
        accountBalances = accountBalances,
        currencySymbol = currencySymbol,
        langCode = settings.language.code,
        onDismiss = { collectingReceivable = null },
        onConfirm = { amount, method, note, dateMillis ->
          viewModel.collectReceivable(activeReceivableToCollect, amount, method, note, dateMillis)
          collectingReceivable = null
        },
      )
    }

    if (isLocked) {
      PinLockOverlay(onVerifyPin = viewModel::unlockWithPin)
    }
  }
}
