package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.viewmodel.DashboardUiData
import com.example.viewmodel.HisabAnalytics

private enum class LedgerTypeFilter {
  ALL,
  RECEIPTS,
  PAYMENTS,
  TRANSFERS,
}

@Composable
fun LedgerScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  selectedPeriod: PeriodFilter,
  selectedMonthKey: String,
  onSelectPeriod: (PeriodFilter) -> Unit,
  onShiftMonth: (Int) -> Unit,
  onExportLedgerCsv: (selectedAccountKey: String?) -> Unit,
) {
  val strings = LocalStrings.current
  val symbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  val langCode = settings.language.code

  var selectedAccountKey by remember { mutableStateOf<String?>(null) }
  var accountMenuExpanded by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var typeFilter by remember { mutableStateOf(LedgerTypeFilter.ALL) }

  when (uiState) {
    UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
      Text(uiState.message, color = MaterialTheme.colorScheme.error)
    }
    is UiState.Success -> {
      val data = uiState.data
      val accountSummaries = remember(data, strings, settings.currencyCode) {
        data.resolveAccountSummaries(strings, settings.currencyCode)
      }
      val (startMillis, endMillis) = remember(selectedPeriod, selectedMonthKey) {
        DateFormatUtils.periodRangeMillis(selectedPeriod, selectedMonthKey)
      }
      val ledger = remember(data, selectedAccountKey, startMillis, endMillis, strings) {
        HisabAnalytics.computeAccountLedger(data, selectedAccountKey, startMillis, endMillis, strings)
      }
      val filteredEntries = remember(ledger.entries, searchQuery, typeFilter, symbol) {
        val q = searchQuery.trim().lowercase()
        ledger.entries.filter { entry ->
          val typeMatches = when (typeFilter) {
            LedgerTypeFilter.ALL -> true
            LedgerTypeFilter.RECEIPTS -> entry.receiptAmount > 0.0
            LedgerTypeFilter.PAYMENTS -> entry.paymentAmount > 0.0
            LedgerTypeFilter.TRANSFERS -> entry.entityType == "TRANSFER"
          }
          val queryMatches = if (q.isEmpty()) true else {
            val amountVal = if (entry.receiptAmount > 0.0) entry.receiptAmount else entry.paymentAmount
            entry.title.lowercase().contains(q) ||
              entry.accountName.lowercase().contains(q) ||
              entry.categoryOrType.lowercase().contains(q) ||
              CurrencyFormat.format(amountVal, symbol).lowercase().contains(q)
          }
          typeMatches && queryMatches
        }
      }
      val selectedAccountLabel = selectedAccountKey?.let { key ->
        accountSummaries.firstOrNull { it.accountId == key }?.name
      } ?: strings.allAccountsOption

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("ledger_lazy_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(modifier = Modifier.weight(1f)) {
              OutlinedButton(
                onClick = { accountMenuExpanded = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("ledger_account_selector"),
              ) {
                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = strings.accountsLabel, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                  text = selectedAccountLabel,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                )
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
              }
              DropdownMenu(
                expanded = accountMenuExpanded,
                onDismissRequest = { accountMenuExpanded = false },
              ) {
                DropdownMenuItem(
                  text = { Text(strings.allAccountsOption) },
                  onClick = {
                    selectedAccountKey = null
                    if (typeFilter == LedgerTypeFilter.TRANSFERS) {
                      typeFilter = LedgerTypeFilter.ALL
                    }
                    accountMenuExpanded = false
                  },
                )
                accountSummaries.forEach { summary ->
                  DropdownMenuItem(
                    text = {
                      Text("${summary.name} (${CurrencyFormat.formatSigned(summary.currentBalance, symbol)})")
                    },
                    onClick = { selectedAccountKey = summary.accountId; accountMenuExpanded = false },
                  )
                }
              }
            }
            FilledTonalButton(
              onClick = { onExportLedgerCsv(selectedAccountKey) },
              modifier = Modifier
                .height(48.dp)
                .testTag("export_ledger_csv_button"),
            ) {
              Icon(Icons.Rounded.Download, contentDescription = strings.exportLedgerCsv, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(4.dp))
              Text("CSV")
            }
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

        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text(strings.searchLedgerHint) },
              leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.searchLedgerHint) },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("ledger_search_input"),
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              val chips = buildList {
                add(LedgerTypeFilter.ALL to strings.allFilterTab)
                add(LedgerTypeFilter.RECEIPTS to strings.receiptsLabel)
                add(LedgerTypeFilter.PAYMENTS to strings.paymentsLabel)
                if (selectedAccountKey != null) {
                  add(LedgerTypeFilter.TRANSFERS to strings.entryTypeTransfer)
                }
              }
              chips.forEach { (filter, label) ->
                FilterChip(
                  selected = typeFilter == filter,
                  onClick = { typeFilter = filter },
                  label = { Text(label) },
                  modifier = Modifier.testTag("ledger_filter_${filter.name}"),
                )
              }
            }
          }
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              LedgerKpiCard(
                label = strings.receiptsLabel,
                amount = ledger.totalReceipts,
                symbol = symbol,
                tint = SemanticSafeLight,
                blurBalances = settings.blurBalances,
                modifier = Modifier.weight(1f),
              )
              LedgerKpiCard(
                label = strings.paymentsLabel,
                amount = ledger.totalPayments,
                symbol = symbol,
                tint = SemanticDangerLight,
                blurBalances = settings.blurBalances,
                modifier = Modifier.weight(1f),
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              LedgerKpiCard(
                label = strings.netChangeLabel,
                amount = ledger.netChange,
                symbol = symbol,
                tint = if (ledger.netChange >= 0.0) SemanticSafeLight else SemanticDangerLight,
                blurBalances = settings.blurBalances,
                signed = true,
                modifier = Modifier.weight(1f),
              )
              LedgerKpiCard(
                label = strings.closingBalanceLabel,
                amount = ledger.closingBalance,
                symbol = symbol,
                tint = MaterialTheme.colorScheme.primary,
                blurBalances = settings.blurBalances,
                signed = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        item {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.openingBalanceLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
              SensitiveAmountText(
                text = CurrencyFormat.formatSigned(ledger.openingBalance, symbol),
                blurBalances = settings.blurBalances,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
          }
        }

        if (filteredEntries.isEmpty()) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(Icons.Rounded.MenuBook, contentDescription = strings.navLedger, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                Text(strings.noDataForMonth, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        } else {
          items(filteredEntries.reversed()) { entry ->
            val isReceipt = entry.receiptAmount > 0.0
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isReceipt) SemanticSafeLight else SemanticDangerLight),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = "${entry.accountName} • ${entry.categoryOrType} • ${DateFormatUtils.formatDateCompact(entry.dateMillis, langCode)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                  SensitiveAmountText(
                    text = if (isReceipt) {
                      "+${CurrencyFormat.format(entry.receiptAmount, symbol)}"
                    } else {
                      "-${CurrencyFormat.format(entry.paymentAmount, symbol)}"
                    },
                    blurBalances = settings.blurBalances,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isReceipt) SemanticSafeLight else SemanticDangerLight,
                  )
                  SensitiveAmountText(
                    text = "${strings.runningBalanceLabel}: ${CurrencyFormat.formatSigned(entry.runningBalance, symbol)}",
                    blurBalances = settings.blurBalances,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LedgerKpiCard(
  label: String,
  amount: Double,
  symbol: String,
  tint: Color,
  blurBalances: Boolean,
  signed: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape = RoundedCornerShape(16.dp),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      SensitiveAmountText(
        text = if (signed) CurrencyFormat.formatSigned(amount, symbol) else CurrencyFormat.format(amount, symbol),
        blurBalances = blurBalances,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = tint,
      )
    }
  }
}
