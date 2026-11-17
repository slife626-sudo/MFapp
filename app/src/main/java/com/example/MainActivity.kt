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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
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
import com.example.data.model.CategoryCatalog
import com.example.data.model.CurrencyOption
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.UiState
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.prefs.UserSettings
import com.example.data.repository.ExpenseRepository
import com.example.i18n.LocalStrings
import com.example.i18n.stringsFor
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.components.AddEditExpenseSheet
import com.example.ui.components.PinLockOverlay
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HisabTheme
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
        val prefsRepo = UserPreferencesRepository(app.applicationContext)
        HisabViewModel(
          repository = repository,
          prefsRepo = prefsRepo,
          appContext = app.applicationContext,
          currentUserId = currentUser.uid,
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
  var openSheetAsIncome by remember { mutableStateOf(false) }

  val destinations = remember(strings) {
    listOf(
      NavDestination(ROUTE_DASHBOARD, strings.navExpenses, Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong),
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

  val customCategories = (uiState as? UiState.Success)?.data?.customCategories.orEmpty()
  val categories = remember(strings, customCategories) {
    CategoryCatalog.mergeCategories(strings, customCategories)
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
                  label = { Text(dest.label) },
                  modifier = Modifier.testTag("nav_${dest.route}"),
                )
              }
            }
          }
        },
        floatingActionButton = {
          if (currentRoute == ROUTE_DASHBOARD || currentRoute == ROUTE_REPORTS) {
            FloatingActionButton(
              onClick = {
                editingExpense = null
                editingIncome = null
                openSheetAsIncome = false
                showExpenseSheet = true
              },
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
                    editingIncome = null
                    editingExpense = exp
                    openSheetAsIncome = false
                    showExpenseSheet = true
                  },
                  onDeleteExpense = { exp -> viewModel.deleteExpense(exp.id) },
                  onNavigateToBudgets = { navigateTo(ROUTE_BUDGETS) },
                  onAddBalance = {
                    editingExpense = null
                    editingIncome = null
                    openSheetAsIncome = true
                    showExpenseSheet = true
                  },
                  onEditIncome = { inc ->
                    editingExpense = null
                    editingIncome = inc
                    openSheetAsIncome = true
                    showExpenseSheet = true
                  },
                  onDeleteIncome = { inc -> viewModel.deleteIncome(inc.id) },
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
        initialIsIncome = openSheetAsIncome,
        categories = categories,
        currencySymbol = currencySymbol,
        langCode = settings.language.code,
        onDismiss = {
          showExpenseSheet = false
          editingExpense = null
          editingIncome = null
          openSheetAsIncome = false
        },
        onSaveExpense = { title, amount, categoryId, method, note, dateMillis ->
          val existing = editingExpense
          if (existing == null) {
            viewModel.addExpense(title, amount, categoryId, method, note, dateMillis)
          } else {
            viewModel.updateExpense(existing.id, title, amount, categoryId, method, note, dateMillis)
          }
          showExpenseSheet = false
          editingExpense = null
          editingIncome = null
          openSheetAsIncome = false
        },
        onSaveIncome = { title, amount, method, note, dateMillis ->
          val existing = editingIncome
          if (existing == null) {
            viewModel.addIncome(title, amount, method, note, dateMillis)
          } else {
            viewModel.updateIncome(existing.id, title, amount, method, note, dateMillis)
          }
          showExpenseSheet = false
          editingExpense = null
          editingIncome = null
          openSheetAsIncome = false
        },
        onCreateCustomCategory = viewModel::addCustomCategory,
      )
    }

    if (isLocked) {
      PinLockOverlay(onVerifyPin = viewModel::unlockWithPin)
    }
  }
}
