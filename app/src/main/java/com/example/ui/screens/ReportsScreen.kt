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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.DateFormatUtils
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.LocalStrings
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DailySpendingBarChart
import com.example.ui.components.SixMonthComparisonChart
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.viewmodel.CategoryBreakdown
import com.example.viewmodel.DashboardUiData
import com.example.viewmodel.MonthlySummary

@Composable
fun ReportsScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  selectedMonthKey: String,
  onShiftMonth: (Int) -> Unit,
  computeSummary: (DashboardUiData) -> MonthlySummary,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol

  when (uiState) {
    UiState.Loading -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }

    is UiState.Error -> {
      Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
      }
    }

    is UiState.Success -> {
      val summary = remember(uiState.data, selectedMonthKey, settings.language) {
        computeSummary(uiState.data)
      }

      LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = strings.monthlyReportTitle,
              style = MaterialTheme.typography.headlineMedium,
            )

            Surface(
              shape = RoundedCornerShape(50),
              color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = { onShiftMonth(-1) },
                  modifier = Modifier.testTag("report_prev_month"),
                ) {
                  Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Prev")
                }
                Text(
                  text = DateFormatUtils.formatMonthLabel(selectedMonthKey, settings.language.code),
                  style = MaterialTheme.typography.labelLarge,
                )
                IconButton(
                  onClick = { onShiftMonth(1) },
                  modifier = Modifier.testTag("report_next_month"),
                ) {
                  Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next")
                }
              }
            }
          }
        }

        // KPI Grid
        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              val deltaPct = if (summary.prevMonthSpent > 0) {
                (((summary.totalSpent - summary.prevMonthSpent) / summary.prevMonthSpent) * 100).toInt()
              } else null
              KpiTile(
                label = strings.totalSpent,
                value = CurrencyFormat.format(summary.totalSpent, currencySymbol),
                subtitle = if (deltaPct != null) {
                  val sign = if (deltaPct >= 0) "+" else ""
                  "$sign$deltaPct% ${strings.vsPrevMonth}"
                } else null,
                subtitleColor = if ((deltaPct ?: 0) > 0) SemanticDangerLight else SemanticSafeLight,
                modifier = Modifier.weight(1f),
              )
              KpiTile(
                label = strings.dailyAverage,
                value = CurrencyFormat.format(summary.dailyAverage, currencySymbol),
                modifier = Modifier.weight(1f),
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              KpiTile(
                label = strings.transactionsCount,
                value = summary.transactionCount.toString(),
                modifier = Modifier.weight(1f),
              )
              KpiTile(
                label = strings.topCategory,
                value = summary.topCategory?.category?.name ?: "—",
                subtitle = summary.topCategory?.let {
                  CurrencyFormat.format(it.totalAmount, currencySymbol)
                },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        if (summary.transactionCount == 0) {
          item {
            Card(
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              ),
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Icon(
                  imageVector = Icons.Rounded.BarChart,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(44.dp),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                  text = strings.noDataForMonth,
                  style = MaterialTheme.typography.titleMedium,
                )
              }
            }
          }
        } else {
          // 1. Donut Chart Card
          item {
            ChartSectionCard(title = strings.categoryChartTitle) {
              CategoryDonutChart(
                breakdowns = summary.categoryBreakdowns,
                totalSpent = summary.totalSpent,
                currencySymbol = currencySymbol,
                totalLabel = strings.totalSpent,
              )
            }
          }

          // 2. Daily Trend Bar Chart Card
          item {
            ChartSectionCard(title = strings.dailyTrendTitle) {
              DailySpendingBarChart(
                monthKey = selectedMonthKey,
                dailyTotals = summary.dailyTotals,
                budgetLimit = summary.budgetLimit,
                currencySymbol = currencySymbol,
                tapHint = strings.tapBarHint,
              )
            }
          }
        }

        // 3. 6-Month Comparison Chart (always visible so users see multi-month history)
        item {
          ChartSectionCard(title = strings.sixMonthChartTitle) {
            SixMonthComparisonChart(
              sixMonthTotals = summary.sixMonthTotals,
              currencySymbol = currencySymbol,
            )
          }
        }

        if (summary.categoryBreakdowns.isNotEmpty()) {
          item {
            Text(
              text = strings.categoryBreakdown,
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(top = 4.dp),
            )
          }

          items(summary.categoryBreakdowns, key = { it.category.id }) { item ->
            CategoryBreakdownRow(item = item, currencySymbol = currencySymbol)
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
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
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
      Text(
        text = value,
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
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
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(22.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(14.dp))
      content()
    }
  }
}

@Composable
private fun CategoryBreakdownRow(
  item: CategoryBreakdown,
  currencySymbol: String,
) {
  val catColor = Color(item.category.colorHex)
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(catColor.copy(alpha = 0.14f)),
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
          Text(
            text = item.category.name,
            style = MaterialTheme.typography.titleMedium,
          )
          Text(
            text = "${item.transactionCount} • ${(item.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          text = CurrencyFormat.format(item.totalAmount, currencySymbol),
          fontFamily = HisabMonoFontFamily,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      Spacer(Modifier.height(10.dp))
      LinearProgressIndicator(
        progress = { item.percentage.coerceIn(0f, 1f) },
        color = catColor,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
      )
    }
  }
}
