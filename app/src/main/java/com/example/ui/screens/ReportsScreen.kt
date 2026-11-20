package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryCatalog
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.PeriodFilter
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.LocalStrings
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DailySpendingBarChart
import com.example.ui.components.IncomeVsExpenseChart
import com.example.ui.components.NetCashFlowAreaChart
import com.example.ui.components.PeriodFilterStrip
import com.example.ui.components.SensitiveAmountText
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.viewmodel.CategoryBreakdown
import com.example.viewmodel.DashboardUiData
import com.example.viewmodel.HisabAnalytics
import com.example.viewmodel.MonthlySummary

@Composable
fun ReportsScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  selectedMonthKey: String,
  onShiftMonth: (Int) -> Unit,
  computeSummary: (DashboardUiData) -> MonthlySummary,
  selectedPeriod: PeriodFilter = PeriodFilter.THIS_MONTH,
  onSelectPeriod: (PeriodFilter) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  var categoryModeTab by remember { mutableIntStateOf(0) } // 0 = Expense, 1 = Income

  when (uiState) {
    UiState.Loading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }

    is UiState.Error -> Box(
      modifier = modifier.fillMaxSize().padding(24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
    }

    is UiState.Success -> {
      val legacySummary = remember(uiState.data, selectedMonthKey, settings.language) {
        computeSummary(uiState.data)
      }
      val periodSummary = remember(uiState.data, selectedPeriod, selectedMonthKey, settings.language) {
        HisabAnalytics.computePeriodAnalytics(
          data = uiState.data,
          filter = selectedPeriod,
          customMonthKey = selectedMonthKey,
          language = settings.language,
        )
      }

      val activeBreakdowns = if (categoryModeTab == 0) periodSummary.expenseBreakdowns else periodSummary.incomeBreakdowns
      val activeCategoryTotal = if (categoryModeTab == 0) periodSummary.totalSpent else periodSummary.totalIncome

      LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Text(
            text = strings.monthlyReportTitle,
            style = MaterialTheme.typography.headlineMedium,
          )
        }

        item {
          PeriodFilterStrip(
            selectedPeriod = selectedPeriod,
            selectedMonthKey = selectedMonthKey,
            langCode = settings.language.code,
            onSelectPeriod = onSelectPeriod,
            onShiftMonth = onShiftMonth,
          )
        }

        // KPI Grid
        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              KpiTile(
                label = strings.totalIncomeLabel,
                value = "+${CurrencyFormat.format(periodSummary.totalIncome, currencySymbol)}",
                blurBalances = settings.blurBalances,
                valueColor = SemanticSafeLight,
                modifier = Modifier.weight(1f),
              )
              val deltaPct = if (periodSummary.prevPeriodSpent > 0.0) {
                (((periodSummary.totalSpent - periodSummary.prevPeriodSpent) / periodSummary.prevPeriodSpent) * 100).toInt()
              } else null
              KpiTile(
                label = strings.totalSpent,
                value = "-${CurrencyFormat.format(periodSummary.totalSpent, currencySymbol)}",
                blurBalances = settings.blurBalances,
                valueColor = SemanticDangerLight,
                subtitle = if (deltaPct != null) {
                  val sign = if (deltaPct >= 0) "+" else ""
                  "$sign$deltaPct% ${strings.vsPrevMonth}"
                } else null,
                subtitleColor = if ((deltaPct ?: 0) > 0) SemanticDangerLight else SemanticSafeLight,
                modifier = Modifier.weight(1f),
              )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              KpiTile(
                label = strings.netIncomeLabel,
                value = CurrencyFormat.formatSigned(periodSummary.netIncome, currencySymbol),
                blurBalances = settings.blurBalances,
                valueColor = if (periodSummary.netIncome >= 0.0) SemanticSafeLight else SemanticDangerLight,
                modifier = Modifier.weight(1f),
              )
              val ratePct = (periodSummary.savingsRate * 100f).toInt()
              KpiTile(
                label = strings.savingsRateLabel,
                value = "$ratePct%",
                blurBalances = false,
                valueColor = if (ratePct >= 0) SemanticSafeLight else SemanticDangerLight,
                modifier = Modifier.weight(1f),
              )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              KpiTile(
                label = strings.dailyAverage,
                value = CurrencyFormat.format(periodSummary.dailyAverage, currencySymbol),
                blurBalances = settings.blurBalances,
                modifier = Modifier.weight(1f),
              )
              KpiTile(
                label = strings.transactionsCount,
                value = periodSummary.transactionCount.toString(),
                blurBalances = false,
                subtitle = periodSummary.topCategory?.let {
                  "${it.category.name} (${CurrencyFormat.format(it.totalAmount, currencySymbol)})"
                },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        if (periodSummary.transactionCount == 0 && legacySummary.transactionCount == 0) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                Spacer(Modifier.height(10.dp))
                Text(strings.noDataForMonth, style = MaterialTheme.typography.titleMedium)
              }
            }
          }
        } else {
          // 1. Donut Chart Card with Expense / Income toggle
          item {
            ChartSectionCard(title = strings.categoryChartTitle) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                FilterChip(
                  selected = categoryModeTab == 0,
                  onClick = { categoryModeTab = 0 },
                  label = { Text(strings.expenseCategoryType) },
                  modifier = Modifier.testTag("reports_donut_expense_tab"),
                )
                FilterChip(
                  selected = categoryModeTab == 1,
                  onClick = { categoryModeTab = 1 },
                  label = { Text(strings.incomeCategoryType) },
                  modifier = Modifier.testTag("reports_donut_income_tab"),
                )
              }
              CategoryDonutChart(
                breakdowns = activeBreakdowns,
                totalSpent = activeCategoryTotal,
                currencySymbol = currencySymbol,
                totalLabel = if (categoryModeTab == 0) strings.totalSpent else strings.totalIncomeLabel,
              )
            }
          }

          // 2. Daily Trend Bar Chart
          if (periodSummary.dailyTotals.isNotEmpty()) {
            item {
              ChartSectionCard(title = strings.dailyTrendTitle) {
                DailySpendingBarChart(
                  monthKey = periodSummary.monthKey,
                  dailyTotals = periodSummary.dailyTotals,
                  budgetLimit = periodSummary.budgetLimit,
                  currencySymbol = currencySymbol,
                  tapHint = strings.tapBarHint,
                )
              }
            }
          }
        }

        // 3. Income vs Expense 6-Month Chart
        item {
          ChartSectionCard(title = strings.incomeVsExpenseTitle) {
            IncomeVsExpenseChart(
              series = periodSummary.incomeVsExpenseMonthly,
              currencySymbol = currencySymbol,
              incomeLabel = strings.totalIncomeLabel,
              expenseLabel = strings.totalSpent,
              barLabel = strings.barChartLabel,
              lineLabel = strings.lineChartLabel,
              blurBalances = settings.blurBalances,
            )
          }
        }

        // 4. Net Cash Flow Area Chart
        item {
          ChartSectionCard(title = strings.netCashFlowTitle) {
            NetCashFlowAreaChart(
              series = periodSummary.netCashFlowSeries,
              currencySymbol = currencySymbol,
              surplusLabel = strings.surplusLabel,
              deficitLabel = strings.deficitLabel,
              blurBalances = settings.blurBalances,
            )
          }
        }

        if (activeBreakdowns.isNotEmpty()) {
          item {
            Text(
              text = strings.categoryBreakdown,
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(top = 4.dp),
            )
          }
          items(activeBreakdowns, key = { "${categoryModeTab}_${it.category.id}" }) { item ->
            CategoryBreakdownRow(
              item = item,
              currencySymbol = currencySymbol,
              blurBalances = settings.blurBalances,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun KpiTile(
  label: String,
  value: String,
  blurBalances: Boolean,
  modifier: Modifier = Modifier,
  valueColor: Color = MaterialTheme.colorScheme.onSurface,
  subtitle: String? = null,
  subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    shape = RoundedCornerShape(18.dp),
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(Modifier.height(4.dp))
      SensitiveAmountText(
        text = value,
        blurBalances = blurBalances,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = valueColor,
      )
      if (subtitle != null) {
        Spacer(Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = subtitleColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun ChartSectionCard(
  title: String,
  content: @Composable () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape = RoundedCornerShape(22.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
      Spacer(Modifier.height(14.dp))
      content()
    }
  }
}

@Composable
private fun CategoryBreakdownRow(
  item: CategoryBreakdown,
  currencySymbol: String,
  blurBalances: Boolean,
) {
  val catColor = Color(item.category.colorHex)
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier.size(38.dp).clip(CircleShape).background(catColor.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = CategoryCatalog.iconFor(item.category.iconKey),
            contentDescription = item.category.name,
            tint = catColor,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(item.category.name, style = MaterialTheme.typography.titleMedium)
          Text(
            text = "${item.transactionCount} • ${(item.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        SensitiveAmountText(
          text = CurrencyFormat.format(item.totalAmount, currencySymbol),
          blurBalances = blurBalances,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      Spacer(Modifier.height(10.dp))
      LinearProgressIndicator(
        progress = { item.percentage.coerceIn(0f, 1f) },
        color = catColor,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
      )
    }
  }
}
