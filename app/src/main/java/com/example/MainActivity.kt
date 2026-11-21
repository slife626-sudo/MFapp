package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryFlowType
import com.example.data.model.CurrencyOption
import com.example.data.model.CustomAccount
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.Payable
import com.example.data.model.Receivable
import com.example.data.model.UiState
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.prefs.UserSettings
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MarketListRepository
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.i18n.stringsFor
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.components.AddEditAccountSheet
import com.example.ui.components.AddEditExpenseSheet
import com.example.ui.components.CollectReceivableDialog
import com.example.ui.components.EntrySheetMode
import com.example.ui.components.HisabRibbonBottomBar
import com.example.ui.components.PinLockOverlay
import com.example.ui.components.RepayPayableDialog
import com.example.ui.components.RibbonNavItem
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
import com.example.ui.screens.TxFilterMode
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.HisabTheme
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.AccountBalances
import com.example.viewmodel.HisabViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_TRANSACTIONS = "transactions"
const val ROUTE_EXPENSES_LIST = "expenses_list"
const val ROUTE_INCOMES_LIST = "incomes_list"
const val ROUTE_WALLETS = "wallets"
const val ROUTE_BUDGETS = "budgets"
const val ROUTE_CATEGORIES = "categories"
const val ROUTE_MARKET = "market"
const val ROUTE_LOANS = "loans"
const val ROUTE_REPORTS = "reports"
const val ROUTE_LEDGER = "ledger"
const val ROUTE_TRIAL_BALANCE = "trial_balance"
const val ROUTE_TRASH = "trash"
const val ROUTE_SETTINGS = "settings"

private data class NavDestination(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private data class DrawerNavItem(
  val route: String,
  val label: String,
  val icon: ImageVector,
  val badgeCount: Int = 0,
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
    if (FirebaseApp.getApps(this).isEmpty()) {
      FirebaseApp.initializeApp(this)
    }
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
  onSelectLanguage: (AppLanguage) -> Unit,
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

@OptIn(ExperimentalMaterial3Api::class)
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
  val selectedPeriodFilter by viewModel.selectedPeriodFilter.collectAsStateWithLifecycle()
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
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  var showExpenseSheet by remember { mutableStateOf(false) }
  var editingExpense by remember { mutableStateOf<Expense?>(null) }
  var editingIncome by remember { mutableStateOf<Income?>(null) }
  var editingTransfer by remember { mutableStateOf<AccountTransfer?>(null) }
  var editingPayable by remember { mutableStateOf<Payable?>(null) }
  var editingReceivable by remember { mutableStateOf<Receivable?>(null) }
  var sheetInitialMode by remember { mutableStateOf(EntrySheetMode.EXPENSE) }
  var repayingPayable by remember { mutableStateOf<Payable?>(null) }
  var collectingReceivable by remember { mutableStateOf<Receivable?>(null) }

  var showAccountSheet by remember { mutableStateOf(false) }
  var editingAccount by remember { mutableStateOf<CustomAccount?>(null) }
  var pendingLedgerExportAccountKey by remember { mutableStateOf<String?>(null) }

  val ledgerCsvLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("text/csv"),
  ) { uri ->
    if (uri != null) {
      val csv = viewModel.buildLedgerCsvString(
        selectedAccountKey = pendingLedgerExportAccountKey,
        periodFilter = selectedPeriodFilter,
        monthKey = selectedMonthKey,
      )
      runCatching {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(csv) }
      }.onSuccess {
        viewModel.notifyMessage(strings.exportSuccess)
      }.onFailure { err ->
        viewModel.notifyMessage(err.localizedMessage ?: "Export failed")
      }
    }
  }

  val trialBalanceCsvLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("text/csv"),
  ) { uri ->
    if (uri != null) {
      val csv = viewModel.buildTrialBalanceCsvString(
        periodFilter = selectedPeriodFilter,
        monthKey = selectedMonthKey,
      )
      runCatching {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(csv) }
      }.onSuccess {
        viewModel.notifyMessage(strings.exportSuccess)
      }.onFailure { err ->
        viewModel.notifyMessage(err.localizedMessage ?: "Export failed")
      }
    }
  }

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

  fun navigateTo(route: String) {
    navController.navigate(route) {
      popUpTo(ROUTE_DASHBOARD) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BackHandler(enabled = drawerState.isOpen) {
    scope.launch { drawerState.close() }
  }

  BackHandler(enabled = !drawerState.isOpen && currentRoute != ROUTE_DASHBOARD) {
    navigateTo(ROUTE_DASHBOARD)
  }

  val destinations = remember(strings) {
    listOf(
      NavDestination(ROUTE_DASHBOARD, strings.navDashboard, Icons.Rounded.GridView, Icons.Outlined.GridView),
      NavDestination(ROUTE_TRANSACTIONS, strings.navTransactions, Icons.Rounded.SwapHoriz, Icons.Outlined.SwapHoriz),
      NavDestination(ROUTE_BUDGETS, strings.navBudgets, Icons.Rounded.Payments, Icons.Outlined.Payments),
      NavDestination(ROUTE_WALLETS, strings.navAccounts, Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
      NavDestination(ROUTE_CATEGORIES, strings.navCategories, Icons.Rounded.LocalOffer, Icons.Outlined.LocalOffer),
    )
  }
  val ribbonItems = remember(destinations) {
    destinations.map { d ->
      RibbonNavItem(
        route = d.route,
        label = d.label,
        selectedIcon = d.selectedIcon,
        unselectedIcon = d.unselectedIcon,
      )
    }
  }

  val successData = (uiState as? UiState.Success)?.data
  val customCategories = successData?.customCategories.orEmpty()
  val categories = remember(strings, customCategories) {
    CategoryCatalog.mergeCategories(strings, customCategories)
  }
  val incomeCategories = remember(strings, customCategories) {
    CategoryCatalog.mergeCategories(strings, customCategories, includeIncome = true)
      .filter { it.flowType == CategoryFlowType.INCOME }
  }
  val accountBalances = remember(successData) {
    successData?.accountBalances() ?: AccountBalances()
  }
  val accountSummaries = remember(successData, strings, settings.currencyCode) {
    successData?.resolveAccountSummaries(strings, settings.currencyCode).orEmpty()
  }
  val trashCount = successData?.trashItems?.size ?: 0
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol

  val systemDark = isSystemInDarkTheme()
  val isDarkEffective = when (settings.themeMode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> systemDark
  }

  val topBarTitle = when (currentRoute) {
    ROUTE_DASHBOARD -> strings.navDashboard
    ROUTE_TRANSACTIONS -> strings.navTransactions
    ROUTE_EXPENSES_LIST -> strings.navExpensesList
    ROUTE_INCOMES_LIST -> strings.navIncomesList
    ROUTE_WALLETS -> strings.navAccounts
    ROUTE_BUDGETS -> strings.navBudgets
    ROUTE_CATEGORIES -> strings.navCategories
    ROUTE_MARKET -> strings.marketListTitle
    ROUTE_LOANS -> strings.navLoansDebts
    ROUTE_REPORTS -> strings.navReports
    ROUTE_LEDGER -> strings.navLedger
    ROUTE_TRIAL_BALANCE -> strings.navTrialBalance
    ROUTE_TRASH -> strings.navTrash
    ROUTE_SETTINGS -> strings.navSettings
    else -> strings.appName
  }

  val showFab = currentRoute in setOf(
    ROUTE_DASHBOARD,
    ROUTE_TRANSACTIONS,
    ROUTE_EXPENSES_LIST,
    ROUTE_INCOMES_LIST,
    ROUTE_WALLETS,
    ROUTE_LOANS,
    ROUTE_REPORTS,
  )

  val fabDefaultMode = when (currentRoute) {
    ROUTE_INCOMES_LIST -> EntrySheetMode.INCOME
    ROUTE_LOANS -> EntrySheetMode.BORROW
    else -> EntrySheetMode.EXPENSE
  }

  Box(modifier = Modifier.fillMaxSize()) {
    ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet(modifier = Modifier.widthIn(max = 310.dp)) {
          Column(
            modifier = Modifier
              .fillMaxHeight()
              .verticalScroll(rememberScrollState())
              .padding(vertical = 12.dp),
          ) {
            // Drawer Header
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text(
                  text = strings.appName,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) SemanticSafeLight else SemanticWarningLight),
                )
              }
              if (currentUser.displayName?.isNotBlank() == true) {
                Text(
                  text = currentUser.displayName.orEmpty(),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              if (currentUser.email?.isNotBlank() == true) {
                Text(
                  text = currentUser.email.orEmpty(),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            val sections = listOf(
              strings.drawerSectionMain to listOf(
                DrawerNavItem(ROUTE_DASHBOARD, strings.navDashboard, Icons.Rounded.Home),
                DrawerNavItem(ROUTE_TRANSACTIONS, strings.navTransactions, Icons.Rounded.SwapHoriz),
                DrawerNavItem(ROUTE_EXPENSES_LIST, strings.navExpensesList, Icons.Rounded.ReceiptLong),
                DrawerNavItem(ROUTE_INCOMES_LIST, strings.navIncomesList, Icons.Rounded.TrendingUp),
                DrawerNavItem(ROUTE_BUDGETS, strings.navBudgets, Icons.Rounded.AccountBalanceWallet),
                DrawerNavItem(ROUTE_WALLETS, strings.navAccounts, Icons.Rounded.AccountBalance),
                DrawerNavItem(ROUTE_CATEGORIES, strings.navCategories, Icons.Rounded.Category),
                DrawerNavItem(ROUTE_MARKET, strings.marketListTitle, Icons.Rounded.ShoppingBag),
              ),
              strings.drawerSectionLoans to listOf(
                DrawerNavItem(ROUTE_LOANS, strings.navLoansDebts, Icons.Rounded.Handshake),
              ),
              strings.drawerSectionReports to listOf(
                DrawerNavItem(ROUTE_REPORTS, strings.navReports, Icons.Rounded.BarChart),
                DrawerNavItem(ROUTE_LEDGER, strings.navLedger, Icons.Rounded.MenuBook),
                DrawerNavItem(ROUTE_TRIAL_BALANCE, strings.navTrialBalance, Icons.Rounded.Balance),
              ),
              strings.drawerSectionSettings to listOf(
                DrawerNavItem(ROUTE_TRASH, strings.navTrash, Icons.Rounded.DeleteOutline, badgeCount = trashCount),
                DrawerNavItem(ROUTE_SETTINGS, strings.navSettings, Icons.Rounded.Settings),
              ),
            )

            sections.forEachIndexed { idx, (sectionTitle, items) ->
              Text(
                text = sectionTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
              )
              items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationDrawerItem(
                  label = {
                    Text(
                      text = item.label,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  },
                  selected = selected,
                  onClick = {
                    scope.launch { drawerState.close() }
                    navigateTo(item.route)
                  },
                  icon = {
                    Icon(item.icon, contentDescription = item.label)
                  },
                  badge = if (item.badgeCount > 0) {
                    {
                      Badge {
                        Text(item.badgeCount.toString())
                      }
                    }
                  } else null,
                  modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("drawer_${item.route}"),
                )
              }
              if (idx < sections.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
              }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            ) {
              Text(
                text = strings.adFreeBadge,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              )
            }
          }
        }
      },
    ) {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
          contentWindowInsets = WindowInsets.safeDrawing,
          snackbarHost = { SnackbarHost(snackbarHostState) },
          topBar = {
            TopAppBar(
              title = {
                Text(
                  text = topBarTitle,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold,
                )
              },
              navigationIcon = {
                IconButton(
                  onClick = { scope.launch { drawerState.open() } },
                  modifier = Modifier.testTag("open_drawer_button"),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = strings.drawerSectionMain,
                  )
                }
              },
              actions = {
                Surface(
                  onClick = {
                    val nextLang = when (settings.language) {
                      AppLanguage.BENGALI -> AppLanguage.ENGLISH
                      AppLanguage.ENGLISH -> AppLanguage.HINDI
                      AppLanguage.HINDI -> AppLanguage.BENGALI
                    }
                    viewModel.setLanguage(nextLang)
                  },
                  shape = RoundedCornerShape(20.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("quick_language_toggle"),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Language,
                      contentDescription = strings.languageLabel,
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text = when (settings.language) {
                        AppLanguage.BENGALI -> "বাং"
                        AppLanguage.ENGLISH -> "EN"
                        AppLanguage.HINDI -> "हि"
                      },
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
                IconButton(
                  onClick = {
                    viewModel.setThemeMode(if (isDarkEffective) ThemeMode.LIGHT else ThemeMode.DARK)
                  },
                  modifier = Modifier.testTag("quick_theme_toggle"),
                ) {
                  Icon(
                    imageVector = if (isDarkEffective) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = strings.themeModeLabel,
                  )
                }
              },
              colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.surface,
              ),
            )
          },
          bottomBar = {
            if (!useNavRail) {
              HisabRibbonBottomBar(
                items = ribbonItems,
                currentRoute = currentRoute,
                onNavigate = ::navigateTo,
              )
            }
          },
          floatingActionButton = {
            if (showFab) {
              FloatingActionButton(
                onClick = { openNewSheet(fabDefaultMode) },
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
                    label = { Text(dest.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("rail_${dest.route}"),
                  )
                }
                NavigationRailItem(
                  selected = currentRoute == ROUTE_MARKET,
                  onClick = { navigateTo(ROUTE_MARKET) },
                  icon = {
                    Icon(
                      imageVector = Icons.Rounded.ShoppingBag,
                      contentDescription = strings.marketListTitle,
                    )
                  },
                  label = { Text(strings.marketListTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
                    selectedPeriod = selectedPeriodFilter,
                    onSelectPeriod = viewModel::setPeriodFilter,
                    onNavigateToTransactions = { navigateTo(ROUTE_TRANSACTIONS) },
                    onOpenAddAccount = {
                      editingAccount = null
                      showAccountSheet = true
                    },
                  )
                }

                composable(ROUTE_TRANSACTIONS) {
                  TransactionsScreen(
                    uiState = uiState,
                    settings = settings,
                    selectedPeriod = selectedPeriodFilter,
                    selectedMonthKey = selectedMonthKey,
                    onSelectPeriod = viewModel::setPeriodFilter,
                    onShiftMonth = viewModel::shiftMonth,
                    onEditExpense = { exp ->
                      closeSheet()
                      editingExpense = exp
                      showExpenseSheet = true
                    },
                    onEditIncome = { inc ->
                      closeSheet()
                      editingIncome = inc
                      showExpenseSheet = true
                    },
                    onEditTransfer = { tr ->
                      closeSheet()
                      editingTransfer = tr
                      showExpenseSheet = true
                    },
                    onEditPayable = { pay ->
                      closeSheet()
                      editingPayable = pay
                      showExpenseSheet = true
                    },
                    onEditReceivable = { rec ->
                      closeSheet()
                      editingReceivable = rec
                      showExpenseSheet = true
                    },
                    onDeleteSingle = { entityType, id ->
                      when (entityType) {
                        "EXPENSE" -> viewModel.deleteExpense(id)
                        "INCOME" -> viewModel.deleteIncome(id)
                        "TRANSFER" -> viewModel.deleteTransfer(id)
                        "PAYABLE" -> viewModel.deletePayable(id)
                        "PAYABLE_PAYMENT" -> viewModel.deletePayablePaymentById(id)
                        "RECEIVABLE" -> viewModel.deleteReceivable(id)
                        "RECEIVABLE_PAYMENT" -> viewModel.deleteReceivablePaymentById(id)
                      }
                    },
                    onBulkSoftDelete = viewModel::bulkSoftDeleteTransactions,
                    initialTypeFilter = null,
                  )
                }

                composable(ROUTE_EXPENSES_LIST) {
                  TransactionsScreen(
                    uiState = uiState,
                    settings = settings,
                    selectedPeriod = selectedPeriodFilter,
                    selectedMonthKey = selectedMonthKey,
                    onSelectPeriod = viewModel::setPeriodFilter,
                    onShiftMonth = viewModel::shiftMonth,
                    onEditExpense = { exp ->
                      closeSheet()
                      editingExpense = exp
                      showExpenseSheet = true
                    },
                    onEditIncome = { inc ->
                      closeSheet()
                      editingIncome = inc
                      showExpenseSheet = true
                    },
                    onEditTransfer = { tr ->
                      closeSheet()
                      editingTransfer = tr
                      showExpenseSheet = true
                    },
                    onEditPayable = { pay ->
                      closeSheet()
                      editingPayable = pay
                      showExpenseSheet = true
                    },
                    onEditReceivable = { rec ->
                      closeSheet()
                      editingReceivable = rec
                      showExpenseSheet = true
                    },
                    onDeleteSingle = { entityType, id ->
                      when (entityType) {
                        "EXPENSE" -> viewModel.deleteExpense(id)
                        "INCOME" -> viewModel.deleteIncome(id)
                        "TRANSFER" -> viewModel.deleteTransfer(id)
                        "PAYABLE" -> viewModel.deletePayable(id)
                        "PAYABLE_PAYMENT" -> viewModel.deletePayablePaymentById(id)
                        "RECEIVABLE" -> viewModel.deleteReceivable(id)
                        "RECEIVABLE_PAYMENT" -> viewModel.deleteReceivablePaymentById(id)
                      }
                    },
                    onBulkSoftDelete = viewModel::bulkSoftDeleteTransactions,
                    initialTypeFilter = TxFilterMode.EXPENSE,
                  )
                }

                composable(ROUTE_INCOMES_LIST) {
                  TransactionsScreen(
                    uiState = uiState,
                    settings = settings,
                    selectedPeriod = selectedPeriodFilter,
                    selectedMonthKey = selectedMonthKey,
                    onSelectPeriod = viewModel::setPeriodFilter,
                    onShiftMonth = viewModel::shiftMonth,
                    onEditExpense = { exp ->
                      closeSheet()
                      editingExpense = exp
                      showExpenseSheet = true
                    },
                    onEditIncome = { inc ->
                      closeSheet()
                      editingIncome = inc
                      showExpenseSheet = true
                    },
                    onEditTransfer = { tr ->
                      closeSheet()
                      editingTransfer = tr
                      showExpenseSheet = true
                    },
                    onEditPayable = { pay ->
                      closeSheet()
                      editingPayable = pay
                      showExpenseSheet = true
                    },
                    onEditReceivable = { rec ->
                      closeSheet()
                      editingReceivable = rec
                      showExpenseSheet = true
                    },
                    onDeleteSingle = { entityType, id ->
                      when (entityType) {
                        "EXPENSE" -> viewModel.deleteExpense(id)
                        "INCOME" -> viewModel.deleteIncome(id)
                        "TRANSFER" -> viewModel.deleteTransfer(id)
                        "PAYABLE" -> viewModel.deletePayable(id)
                        "PAYABLE_PAYMENT" -> viewModel.deletePayablePaymentById(id)
                        "RECEIVABLE" -> viewModel.deleteReceivable(id)
                        "RECEIVABLE_PAYMENT" -> viewModel.deleteReceivablePaymentById(id)
                      }
                    },
                    onBulkSoftDelete = viewModel::bulkSoftDeleteTransactions,
                    initialTypeFilter = TxFilterMode.INCOME,
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
                    loansOnlyMode = false,
                    onAddAccount = {
                      editingAccount = null
                      showAccountSheet = true
                    },
                    onEditAccount = { acct ->
                      editingAccount = acct
                      showAccountSheet = true
                    },
                    onDeleteAccount = viewModel::deleteAccount,
                    onBulkDeletePayables = { ids ->
                      viewModel.bulkSoftDeleteTransactions(ids.map { "PAYABLE" to it })
                    },
                    onBulkDeleteReceivables = { ids ->
                      viewModel.bulkSoftDeleteTransactions(ids.map { "RECEIVABLE" to it })
                    },
                  )
                }

                composable(ROUTE_LOANS) {
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
                    loansOnlyMode = true,
                    initialDebtTab = 0,
                    onBulkDeletePayables = { ids ->
                      viewModel.bulkSoftDeleteTransactions(ids.map { "PAYABLE" to it })
                    },
                    onBulkDeleteReceivables = { ids ->
                      viewModel.bulkSoftDeleteTransactions(ids.map { "RECEIVABLE" to it })
                    },
                  )
                }

                composable(ROUTE_CATEGORIES) {
                  CategoriesScreen(
                    uiState = uiState,
                    settings = settings,
                    selectedPeriod = selectedPeriodFilter,
                    selectedMonthKey = selectedMonthKey,
                    onSelectPeriod = viewModel::setPeriodFilter,
                    onShiftMonth = viewModel::shiftMonth,
                    onAddCustomCategory = viewModel::addCustomCategory,
                    onUpdateCustomCategory = viewModel::updateCustomCategory,
                    onDeleteCustomCategory = viewModel::deleteCustomCategory,
                    onBulkDeleteCustomCategories = viewModel::bulkDeleteCustomCategories,
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
                    onBackToDashboard = { navigateTo(ROUTE_DASHBOARD) },
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
                    selectedPeriod = selectedPeriodFilter,
                    onSelectPeriod = viewModel::setPeriodFilter,
                  )
                }

                composable(ROUTE_LEDGER) {
                  LedgerScreen(
                    uiState = uiState,
                    settings = settings,
                    selectedPeriod = selectedPeriodFilter,
                    selectedMonthKey = selectedMonthKey,
                    onSelectPeriod = viewModel::setPeriodFilter,
                    onShiftMonth = viewModel::shiftMonth,
                    onExportLedgerCsv = { selectedAcctKey ->
                      pendingLedgerExportAccountKey = selectedAcctKey
                      ledgerCsvLauncher.launch("hisab_ledger_${selectedMonthKey}.csv")
                    },
                  )
                }

                composable(ROUTE_TRIAL_BALANCE) {
                  TrialBalanceScreen(
                    uiState = uiState,
                    settings = settings,
                    selectedPeriod = selectedPeriodFilter,
                    selectedMonthKey = selectedMonthKey,
                    onSelectPeriod = viewModel::setPeriodFilter,
                    onShiftMonth = viewModel::shiftMonth,
                    onExportTrialBalanceCsv = {
                      trialBalanceCsvLauncher.launch("hisab_trial_balance_${selectedMonthKey}.csv")
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

                composable(ROUTE_TRASH) {
                  TrashScreen(
                    uiState = uiState,
                    settings = settings,
                    onRestoreItem = viewModel::restoreTrashItem,
                    onDeleteForever = viewModel::permanentlyDeleteTrashItem,
                    onEmptyTrash = viewModel::emptyTrash,
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
                    trashCount = trashCount,
                    onOpenTrash = { navigateTo(ROUTE_TRASH) },
                    onToggleShowNotes = viewModel::setShowNotesInList,
                    onToggle24HourTime = viewModel::setUse24HourTime,
                    onSelectPageSize = viewModel::setPageSize,
                    onToggleBlurBalances = viewModel::setBlurBalances,
                    onToggleHaptic = viewModel::setHapticEnabled,
                    onClearAllData = viewModel::clearAllUserData,
                    onAddCustomCategoryEx = viewModel::addCustomCategory,
                    onUpdateCustomCategory = viewModel::updateCustomCategory,
                  )
                }
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
        accountSummaries = accountSummaries,
        incomeCategories = incomeCategories,
        use24HourTime = settings.use24HourTime,
        blurBalances = settings.blurBalances,
        hapticEnabled = settings.hapticEnabled,
        onSaveExpenseEx = { title, amount, categoryId, method, note, dateMillis, accountId ->
          val existing = editingExpense
          if (existing == null) {
            viewModel.addExpense(title, amount, categoryId, method, note, dateMillis, accountId)
          } else {
            viewModel.updateExpense(existing.id, title, amount, categoryId, method, note, dateMillis, accountId)
          }
        },
        onSaveIncomeEx = { title, amount, method, note, dateMillis, categoryId, accountId ->
          val existing = editingIncome
          if (existing == null) {
            viewModel.addIncome(title, amount, method, note, dateMillis, categoryId, accountId)
          } else {
            viewModel.updateIncome(existing.id, title, amount, method, note, dateMillis, categoryId, accountId)
          }
        },
        onSaveTransferEx = { fromMethod, toMethod, amount, note, dateMillis, fromAccountId, toAccountId ->
          val existing = editingTransfer
          if (existing == null) {
            viewModel.addTransfer(fromMethod, toMethod, amount, note, dateMillis, fromAccountId, toAccountId)
          } else {
            viewModel.updateTransfer(existing.id, fromMethod, toMethod, amount, note, dateMillis, fromAccountId, toAccountId)
          }
        },
        onSavePayableEx = { personName, amount, method, note, dateMillis, dueDateMillis, accountId ->
          val existing = editingPayable
          if (existing == null) {
            viewModel.addPayable(personName, amount, method, note, dateMillis, dueDateMillis, accountId)
          } else {
            viewModel.updatePayable(existing, personName, amount, method, note, dateMillis, dueDateMillis, accountId)
          }
        },
        onSaveReceivableEx = { personName, amount, method, note, dateMillis, dueDateMillis, accountId ->
          val existing = editingReceivable
          if (existing == null) {
            viewModel.addReceivable(personName, amount, method, note, dateMillis, dueDateMillis, accountId)
          } else {
            viewModel.updateReceivable(existing, personName, amount, method, note, dateMillis, dueDateMillis, accountId)
          }
        },
        onCreateCustomCategoryEx = viewModel::addCustomCategory,
      )
    }

    if (showAccountSheet) {
      val target = editingAccount
      AddEditAccountSheet(
        initialAccount = target,
        currencySymbol = currencySymbol,
        langCode = settings.language.code,
        onDismiss = {
          showAccountSheet = false
          editingAccount = null
        },
        onSave = { name, accountType, iconKey, colorHex, openingBalance ->
          if (target == null) {
            viewModel.addAccount(name, accountType, iconKey, colorHex, openingBalance)
          } else {
            viewModel.updateAccount(target.id, name, accountType, iconKey, colorHex, openingBalance)
          }
          showAccountSheet = false
          editingAccount = null
        },
        onDelete = if (target != null) {
          {
            viewModel.deleteAccount(target.id)
            showAccountSheet = false
            editingAccount = null
          }
        } else null,
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
        accountSummaries = accountSummaries,
        onConfirmWithAccount = { amount, method, note, dateMillis, accountId ->
          viewModel.repayPayable(activePayableToRepay, amount, method, note, dateMillis, accountId)
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
        accountSummaries = accountSummaries,
        onConfirmWithAccount = { amount, method, note, dateMillis, accountId ->
          viewModel.collectReceivable(activeReceivableToCollect, amount, method, note, dateMillis, accountId)
          collectingReceivable = null
        },
      )
    }

    if (isLocked) {
      PinLockOverlay(onVerifyPin = viewModel::unlockWithPin)
    }
  }
}
