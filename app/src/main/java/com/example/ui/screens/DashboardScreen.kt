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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryItem
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.PaymentMethod
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.DashboardUiData
import java.util.Calendar

const val FILTER_INCOME = "__income__"

private sealed interface LedgerEntry {
  val key: String
  val dateMillis: Long

  data class ExpenseItem(val expense: Expense) : LedgerEntry {
    override val key: String get() = "exp_${expense.id}"
    override val dateMillis: Long get() = expense.dateMillis
  }

  data class IncomeItem(val income: Income) : LedgerEntry {
    override val key: String get() = "inc_${income.id}"
    override val dateMillis: Long get() = income.dateMillis
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
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  var expensePendingDelete by remember { mutableStateOf<Expense?>(null) }
  var incomePendingDelete by remember { mutableStateOf<Income?>(null) }

  when (uiState) {
    UiState.Loading -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }

    is UiState.Error -> {
      Box(
        modifier = modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = uiState.message,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyLarge,
        )
      }
    }

    is UiState.Success -> {
      val data = uiState.data
      val categories = remember(strings, data.customCategories) {
        CategoryCatalog.mergeCategories(strings, data.customCategories)
      }
      val catMap = remember(categories) { categories.associateBy { it.id } }

      val monthExpenses = remember(data.expenses, selectedMonthKey) {
        data.expenses.filter { it.monthKey == selectedMonthKey }
      }
      val monthIncomes = remember(data.incomes, selectedMonthKey) {
        data.incomes.filter { it.monthKey == selectedMonthKey }
      }
      val totalMonthSpent = remember(monthExpenses) {
        monthExpenses.sumOf { it.amount }
      }
      val totalMonthIncome = remember(monthIncomes) {
        monthIncomes.sumOf { it.amount }
      }
      val totalAllIncome = remember(data.incomes) {
        data.incomes.sumOf { it.amount }
      }
      val totalWalletBalance = remember(data.incomes, data.expenses) {
        data.incomes.sumOf { it.amount } - data.expenses.sumOf { it.amount }
      }
      val activeBudget = remember(data.budgets, selectedMonthKey) {
        data.budgets.firstOrNull { it.monthKey == selectedMonthKey }
          ?: data.budgets.firstOrNull { it.monthKey == "default" }
      }
      val budgetLimit = activeBudget?.totalLimit ?: 0.0

      val filteredEntries = remember(monthExpenses, monthIncomes, selectedCategoryFilter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        val expList = if (selectedCategoryFilter == FILTER_INCOME) {
          emptyList()
        } else {
          monthExpenses.filter { exp ->
            val matchesCat = selectedCategoryFilter == null || exp.categoryId == selectedCategoryFilter
            val matchesQuery = q.isEmpty() ||
              exp.title.lowercase().contains(q) ||
              exp.note.lowercase().contains(q)
            matchesCat && matchesQuery
          }.map { LedgerEntry.ExpenseItem(it) }
        }
        val incList = if (selectedCategoryFilter != null && selectedCategoryFilter != FILTER_INCOME) {
          emptyList()
        } else {
          monthIncomes.filter { inc ->
            q.isEmpty() ||
              inc.title.lowercase().contains(q) ||
              inc.note.lowercase().contains(q)
          }.map { LedgerEntry.IncomeItem(it) }
        }
        (expList + incList).sortedByDescending { it.dateMillis }
      }

      val groupedByDay = remember(filteredEntries) {
        filteredEntries.groupBy { DateFormatUtils.formatDayKey(it.dateMillis) }
      }

      LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Sync & Offline Status Header
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
              )
              Text(
                text = strings.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            SyncStatusPill(
              cloudSyncEnabled = settings.cloudSyncEnabled,
              isOnline = isOnline,
              hasPendingWrites = data.hasPendingWrites,
            )
          }
        }

        // Hero Monthly Balance & Budget Card
        item {
          HeroSpendingCard(
            monthKey = selectedMonthKey,
            totalSpent = totalMonthSpent,
            totalIncome = totalMonthIncome,
            hasAnyIncome = totalAllIncome > 0.0,
            totalWalletBalance = totalWalletBalance,
            budgetLimit = budgetLimit,
            currencySymbol = currencySymbol,
            langCode = settings.language.code,
            onPrevMonth = { onShiftMonth(-1) },
            onNextMonth = { onShiftMonth(1) },
            onClickBudget = onNavigateToBudgets,
            onAddBalance = onAddBalance,
          )
        }

        // Search Bar
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

        // Category Filter Strip
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
                Icon(
                  imageVector = Icons.Rounded.AccountBalanceWallet,
                  contentDescription = strings.balanceFilterChip,
                  tint = SemanticSafeLight,
                  modifier = Modifier.size(16.dp),
                )
              },
              label = { Text(strings.balanceFilterChip) },
              modifier = Modifier.testTag("filter_income_chip"),
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
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              ),
              shape = RoundedCornerShape(24.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(28.dp),
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
                Text(
                  text = strings.emptyExpensesTitle,
                  style = MaterialTheme.typography.titleMedium,
                )
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
            val daySpentSum = dayEntries.filterIsInstance<LedgerEntry.ExpenseItem>().sumOf { it.expense.amount }
            val dayIncomeSum = dayEntries.filterIsInstance<LedgerEntry.IncomeItem>().sumOf { it.income.amount }
            item(key = "header_${DateFormatUtils.formatDayKey(firstMillis)}") {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 6.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = DateFormatUtils.formatDayHeader(
                    firstMillis,
                    strings.today,
                    strings.yesterday,
                    settings.language.code,
                  ),
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  if (dayIncomeSum > 0.0) {
                    Text(
                      text = "+${CurrencyFormat.format(dayIncomeSum, currencySymbol)}",
                      fontFamily = HisabMonoFontFamily,
                      style = MaterialTheme.typography.labelLarge,
                      color = SemanticSafeLight,
                    )
                  }
                  if (daySpentSum > 0.0) {
                    Text(
                      text = "-${CurrencyFormat.format(daySpentSum, currencySymbol)}",
                      fontFamily = HisabMonoFontFamily,
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }
            }

            items(dayEntries, key = { it.key }) { entry ->
              when (entry) {
                is LedgerEntry.ExpenseItem -> {
                  val expense = entry.expense
                  val cat = catMap[expense.categoryId] ?: CategoryItem(
                    id = expense.categoryId,
                    name = expense.categoryId,
                    iconKey = "other",
                    colorHex = 0xFF0891B2,
                  )
                  ExpenseRowCard(
                    expense = expense,
                    category = cat,
                    currencySymbol = currencySymbol,
                    onClick = { onEditExpense(expense) },
                    onDelete = { expensePendingDelete = expense },
                  )
                }

                is LedgerEntry.IncomeItem -> {
                  IncomeRowCard(
                    income = entry.income,
                    currencySymbol = currencySymbol,
                    onClick = { onEditIncome(entry.income) },
                    onDelete = { incomePendingDelete = entry.income },
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  val pending = expensePendingDelete
  if (pending != null) {
    AlertDialog(
      onDismissRequest = { expensePendingDelete = null },
      title = { Text(strings.confirmDeleteTitle) },
      text = { Text("${pending.title} — ${strings.confirmDeleteMessage}") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteExpense(pending)
            expensePendingDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
          Text(strings.delete)
        }
      },
      dismissButton = {
        TextButton(onClick = { expensePendingDelete = null }) {
          Text(strings.cancel)
        }
      },
    )
  }

  val pendingInc = incomePendingDelete
  if (pendingInc != null) {
    AlertDialog(
      onDismissRequest = { incomePendingDelete = null },
      title = { Text(strings.confirmDeleteBalanceTitle) },
      text = { Text("${pendingInc.title} — ${strings.confirmDeleteBalanceMessage}") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteIncome(pendingInc)
            incomePendingDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
          Text(strings.delete)
        }
      },
      dismissButton = {
        TextButton(onClick = { incomePendingDelete = null }) {
          Text(strings.cancel)
        }
      },
    )
  }
}

@Composable
private fun SyncStatusPill(
  cloudSyncEnabled: Boolean,
  isOnline: Boolean,
  hasPendingWrites: Boolean,
) {
  val strings = LocalStrings.current
  val (icon, label, containerColor) = when {
    !cloudSyncEnabled -> Triple(
      Icons.Rounded.CloudOff,
      strings.offlineModeActive,
      MaterialTheme.colorScheme.surfaceContainerHigh,
    )
    !isOnline -> Triple(
      Icons.Rounded.CloudOff,
      strings.offlineCached,
      MaterialTheme.colorScheme.tertiaryContainer,
    )
    hasPendingWrites -> Triple(
      Icons.Rounded.Sync,
      strings.syncingPending,
      MaterialTheme.colorScheme.secondaryContainer,
    )
    else -> Triple(
      Icons.Rounded.CloudDone,
      strings.onlineSynced,
      MaterialTheme.colorScheme.primaryContainer,
    )
  }

  Surface(
    shape = RoundedCornerShape(50),
    color = containerColor,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        modifier = Modifier.size(15.dp),
      )
      Spacer(Modifier.width(5.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
      )
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
  budgetLimit: Double,
  currencySymbol: String,
  langCode: String,
  onPrevMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onClickBudget: () -> Unit,
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
  } else {
    daysInMonth
  }
  val remainingBudget = (budgetLimit - totalSpent)
  val monthRemainingBalance = totalIncome - totalSpent
  val showBalanceMode = totalIncome > 0.0

  Card(
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(primary, primary.copy(alpha = 0.85f), secondary.copy(alpha = 0.78f))
          )
        )
        .padding(20.dp),
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onPrevMonth, modifier = Modifier.testTag("prev_month_btn")) {
            Icon(
              Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
              contentDescription = "Previous month",
              tint = Color.White,
            )
          }
          Text(
            text = DateFormatUtils.formatMonthLabel(monthKey, langCode),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
          )
          IconButton(onClick = onNextMonth, modifier = Modifier.testTag("next_month_btn")) {
            Icon(
              Icons.AutoMirrored.Rounded.KeyboardArrowRight,
              contentDescription = "Next month",
              tint = Color.White,
            )
          }
        }

        Spacer(Modifier.height(6.dp))
        Text(
          text = if (showBalanceMode) strings.remainingBalance else strings.thisMonthSpend,
          style = MaterialTheme.typography.labelMedium,
          color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(4.dp))
        val headlineText = if (showBalanceMode) {
          if (monthRemainingBalance < 0.0) {
            "-${CurrencyFormat.format(-monthRemainingBalance, currencySymbol)}"
          } else {
            CurrencyFormat.format(monthRemainingBalance, currencySymbol)
          }
        } else {
          CurrencyFormat.format(totalSpent, currencySymbol)
        }
        Text(
          text = headlineText,
          fontFamily = HisabMonoFontFamily,
          style = MaterialTheme.typography.displayLarge,
          color = if (showBalanceMode && monthRemainingBalance < 0.0) Color(0xFFFFD6D6) else Color.White,
          fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        if (showBalanceMode) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
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
                  Text(
                    text = "+${CurrencyFormat.format(totalIncome, currencySymbol)}",
                    fontFamily = HisabMonoFontFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBDF2DF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                Icon(
                  imageVector = Icons.Rounded.Add,
                  contentDescription = strings.addBalance,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
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
                Text(
                  text = "-${CurrencyFormat.format(totalSpent, currencySymbol)}",
                  fontFamily = HisabMonoFontFamily,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }
          if (hasAnyIncome && kotlin.math.abs(totalWalletBalance - monthRemainingBalance) > 0.01) {
            Spacer(Modifier.height(8.dp))
            val overallStr = if (totalWalletBalance < 0.0) {
              "-${CurrencyFormat.format(-totalWalletBalance, currencySymbol)}"
            } else {
              CurrencyFormat.format(totalWalletBalance, currencySymbol)
            }
            Text(
              text = "${strings.overallWalletBalance}: $overallStr",
              style = MaterialTheme.typography.labelMedium,
              color = Color.White.copy(alpha = 0.9f),
            )
          }
          Spacer(Modifier.height(12.dp))
        } else if (hasAnyIncome) {
          val overallStr = if (totalWalletBalance < 0.0) {
            "-${CurrencyFormat.format(-totalWalletBalance, currencySymbol)}"
          } else {
            CurrencyFormat.format(totalWalletBalance, currencySymbol)
          }
          Text(
            text = "${strings.overallWalletBalance}: $overallStr",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.9f),
          )
          Spacer(Modifier.height(10.dp))
        }

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
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
          )
          Spacer(Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = if (remainingBudget >= 0) {
                "${strings.budgetRemaining}: ${CurrencyFormat.format(remainingBudget, currencySymbol)}"
              } else {
                "${strings.budgetExceededBy}: ${CurrencyFormat.format(-remainingBudget, currencySymbol)}"
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
        }

        if (!showBalanceMode || budgetLimit <= 0.0) {
          if (budgetLimit > 0.0) Spacer(Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            if (!showBalanceMode) {
              Surface(
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable(onClick = onAddBalance)
                  .testTag("add_balance_button"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = strings.addBalance,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(Modifier.width(4.dp))
                  Text(
                    text = strings.addBalance,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                  )
                }
              }
            }
            if (budgetLimit <= 0.0) {
              Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable(onClick = onClickBudget),
              ) {
                Text(
                  text = "${strings.setBudgetHint} →",
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
  }
}

@Composable
private fun IncomeRowCard(
  income: Income,
  currencySymbol: String,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  val methodLabel = when (PaymentMethod.fromWire(income.paymentMethod)) {
    PaymentMethod.CASH -> strings.cash
    PaymentMethod.CARD -> strings.card
    PaymentMethod.MOBILE -> strings.mobileBanking
    PaymentMethod.BANK -> strings.bank
  }

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("income_item_${income.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(SemanticSafeLight.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.ArrowDownward,
          contentDescription = strings.entryTypeIncome,
          tint = SemanticSafeLight,
          modifier = Modifier.size(22.dp),
        )
      }

      Spacer(Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = income.title,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = buildString {
            append(strings.entryTypeIncome)
            append(" • ")
            append(methodLabel)
            if (income.note.isNotBlank()) {
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

      Text(
        text = "+${CurrencyFormat.format(income.amount, currencySymbol)}",
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = SemanticSafeLight,
      )

      IconButton(
        onClick = onDelete,
        modifier = Modifier.testTag("delete_income_${income.id}"),
      ) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = strings.delete,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ExpenseRowCard(
  expense: Expense,
  category: CategoryItem,
  currencySymbol: String,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  val methodLabel = when (PaymentMethod.fromWire(expense.paymentMethod)) {
    PaymentMethod.CASH -> strings.cash
    PaymentMethod.CARD -> strings.card
    PaymentMethod.MOBILE -> strings.mobileBanking
    PaymentMethod.BANK -> strings.bank
  }

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("expense_item_${expense.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(Color(category.colorHex).copy(alpha = 0.14f)),
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
        Text(
          text = expense.title,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = buildString {
            append(category.name)
            append(" • ")
            append(methodLabel)
            if (expense.note.isNotBlank()) {
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

      Text(
        text = "-${CurrencyFormat.format(expense.amount, currencySymbol)}",
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      IconButton(
        onClick = onDelete,
        modifier = Modifier.testTag("delete_expense_${expense.id}"),
      ) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = strings.delete,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
