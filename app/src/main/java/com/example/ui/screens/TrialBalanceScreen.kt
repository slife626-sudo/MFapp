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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.DateFormatUtils
import com.example.data.model.PeriodFilter
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.LocalStrings
import com.example.ui.components.PeriodFilterStrip
import com.example.ui.components.SensitiveAmountText
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.DashboardUiData
import com.example.viewmodel.HisabAnalytics
import com.example.viewmodel.TrialBalanceGroup

@Composable
fun TrialBalanceScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  selectedPeriod: PeriodFilter,
  selectedMonthKey: String,
  onSelectPeriod: (PeriodFilter) -> Unit,
  onShiftMonth: (Int) -> Unit,
  onExportTrialBalanceCsv: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val symbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  val langCode = settings.language.code

  when (uiState) {
    UiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    is UiState.Error -> Box(
      modifier = modifier
        .fillMaxSize()
        .padding(24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(uiState.message, color = MaterialTheme.colorScheme.error)
    }
    is UiState.Success -> {
      val data = uiState.data
      val (startMillis, endMillis) = remember(selectedPeriod, selectedMonthKey) {
        DateFormatUtils.periodRangeMillis(selectedPeriod, selectedMonthKey)
      }
      val trialBalance = remember(data, startMillis, endMillis, strings, settings.currencyCode) {
        HisabAnalytics.computeTrialBalance(
          data = data,
          startMillis = startMillis,
          endMillis = endMillis,
          strings = strings,
          currencyCode = settings.currencyCode,
        )
      }

      LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item {
          PeriodFilterStrip(
            selectedPeriod = selectedPeriod,
            selectedMonthKey = selectedMonthKey,
            langCode = langCode,
            onSelectPeriod = onSelectPeriod,
            onShiftMonth = onShiftMonth,
          )
        }

        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = strings.navTrialBalance,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                val statusColor = if (trialBalance.isBalanced) SemanticSafeLight else SemanticWarningLight
                Surface(
                  color = statusColor.copy(alpha = 0.14f),
                  shape = RoundedCornerShape(50),
                  modifier = Modifier.testTag("trial_balance_status_badge"),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      imageVector = if (trialBalance.isBalanced) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                      contentDescription = if (trialBalance.isBalanced) strings.balancedBadge else strings.unbalancedBadge,
                      tint = statusColor,
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text = if (trialBalance.isBalanced) strings.balancedBadge else strings.unbalancedBadge,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = statusColor,
                    )
                  }
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(
                  onClick = onExportTrialBalanceCsv,
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier
                    .height(48.dp)
                    .testTag("trial_balance_export_csv"),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = strings.exportTrialBalanceCsv,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(Modifier.width(4.dp))
                  Text("CSV")
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Surface(
                  color = MaterialTheme.colorScheme.surfaceContainerLow,
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text = strings.debitLabel,
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SensitiveAmountText(
                      text = CurrencyFormat.format(trialBalance.totalDebit, symbol),
                      blurBalances = settings.blurBalances,
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.Bold,
                      color = SemanticSafeLight,
                    )
                  }
                }
                Surface(
                  color = MaterialTheme.colorScheme.surfaceContainerLow,
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text = strings.creditLabel,
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SensitiveAmountText(
                      text = CurrencyFormat.format(trialBalance.totalCredit, symbol),
                      blurBalances = settings.blurBalances,
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.Bold,
                      color = SemanticDangerLight,
                    )
                  }
                }
              }
            }
          }
        }

        if (trialBalance.groups.isEmpty()) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Balance,
                  contentDescription = strings.navTrialBalance,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(38.dp),
                )
                Text(
                  text = strings.noDataForMonth,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        } else {
          itemsIndexed(
            items = trialBalance.groups,
            key = { idx, group -> "${idx}_${group.groupName}" },
          ) { idx, group ->
            TrialBalanceGroupCard(
              group = group,
              symbol = symbol,
              blurBalances = settings.blurBalances,
              debitHeader = strings.debitLabel,
              creditHeader = strings.creditLabel,
              modifier = Modifier.testTag("trial_balance_group_$idx"),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TrialBalanceGroupCard(
  group: TrialBalanceGroup,
  symbol: String,
  blurBalances: Boolean,
  debitHeader: String,
  creditHeader: String,
  modifier: Modifier = Modifier,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape = RoundedCornerShape(18.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceContainerLow)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = group.groupName,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1.4f),
        )
        Text(
          text = debitHeader,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.End,
          modifier = Modifier.weight(1f),
        )
        Text(
          text = creditHeader,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.End,
          modifier = Modifier.weight(1f),
        )
      }

      group.lines.forEachIndexed { lineIdx, line ->
        if (lineIdx > 0) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        }
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = line.accountHead,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.4f),
          )
          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd,
          ) {
            if (line.debitAmount >= 0.005) {
              SensitiveAmountText(
                text = CurrencyFormat.format(line.debitAmount, symbol),
                blurBalances = blurBalances,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = SemanticSafeLight,
              )
            } else {
              Text(
                text = "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd,
          ) {
            if (line.creditAmount >= 0.005) {
              SensitiveAmountText(
                text = CurrencyFormat.format(line.creditAmount, symbol),
                blurBalances = blurBalances,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = SemanticDangerLight,
              )
            } else {
              Text(
                text = "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}
