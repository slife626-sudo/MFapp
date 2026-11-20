package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountSummary
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.CustomAccount
import com.example.data.model.DateFormatUtils
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.Receivable
import com.example.data.model.ReceivablePayment
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.components.SensitiveAmountText
import com.example.ui.components.TransferRowCard
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.DashboardUiData

private enum class DebtSortOrder {
  NEWEST,
  OLDEST,
  AMOUNT_HIGH,
  REMAINING_HIGH,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WalletsScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  onAddBalance: () -> Unit,
  onOpenTransfer: () -> Unit,
  onOpenBorrow: () -> Unit,
  onEditTransfer: (AccountTransfer) -> Unit,
  onDeleteTransfer: (AccountTransfer) -> Unit,
  onEditPayable: (Payable) -> Unit,
  onDeletePayable: (Payable) -> Unit,
  onRepayPayable: (Payable) -> Unit,
  onDeleteRepayment: (PayablePayment) -> Unit,
  onOpenLend: () -> Unit = {},
  onEditReceivable: (Receivable) -> Unit = {},
  onDeleteReceivable: (Receivable) -> Unit = {},
  onCollectReceivable: (Receivable) -> Unit = {},
  onDeleteCollection: (ReceivablePayment) -> Unit = {},
  loansOnlyMode: Boolean = false,
  initialDebtTab: Int = 0,
  onAddAccount: () -> Unit = {},
  onEditAccount: (CustomAccount) -> Unit = {},
  onDeleteAccount: (String) -> Unit = {},
  onBulkDeletePayables: (List<String>) -> Unit = {},
  onBulkDeleteReceivables: (List<String>) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  val langCode = settings.language.code

  var transferPendingDelete by remember { mutableStateOf<AccountTransfer?>(null) }
  var payablePendingDelete by remember { mutableStateOf<Payable?>(null) }
  var repaymentPendingDelete by remember { mutableStateOf<PayablePayment?>(null) }
  var receivablePendingDelete by remember { mutableStateOf<Receivable?>(null) }
  var collectionPendingDelete by remember { mutableStateOf<ReceivablePayment?>(null) }
  var accountPendingDelete by remember { mutableStateOf<CustomAccount?>(null) }

  var selectedDebtTab by remember(initialDebtTab) { mutableIntStateOf(initialDebtTab) } // 0 = Payables, 1 = Receivables
  LaunchedEffect(initialDebtTab) {
    selectedDebtTab = initialDebtTab
  }

  var debtSortOrder by remember { mutableStateOf(DebtSortOrder.NEWEST) }
  var sortMenuExpanded by remember { mutableStateOf(false) }
  var selectionMode by remember { mutableStateOf(false) }
  var selectedDebtIds by remember { mutableStateOf(emptySet<String>()) }
  var confirmBulkDelete by remember { mutableStateOf(false) }

  when (uiState) {
    UiState.Loading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    is UiState.Error -> Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
      Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
    }
    is UiState.Success -> {
      val data = uiState.data
      val balances = remember(data) { data.accountBalances() }
      val accountSummaries = remember(data, strings, settings.currencyCode) {
        data.resolveAccountSummaries(strings, settings.currencyCode)
      }
      val totalUnpaidPayables = remember(data) { data.totalUnpaidPayables() }
      val totalUnpaidReceivables = remember(data) { data.totalUnpaidReceivables() }
      val nowMillis = System.currentTimeMillis()

      fun sortPayables(list: List<Payable>): List<Payable> = when (debtSortOrder) {
        DebtSortOrder.NEWEST -> list.sortedByDescending { it.dateMillis }
        DebtSortOrder.OLDEST -> list.sortedBy { it.dateMillis }
        DebtSortOrder.AMOUNT_HIGH -> list.sortedByDescending { it.amount }
        DebtSortOrder.REMAINING_HIGH -> list.sortedByDescending { (it.amount - data.paidAmountForPayable(it)).coerceAtLeast(0.0) }
      }
      fun sortReceivables(list: List<Receivable>): List<Receivable> = when (debtSortOrder) {
        DebtSortOrder.NEWEST -> list.sortedByDescending { it.dateMillis }
        DebtSortOrder.OLDEST -> list.sortedBy { it.dateMillis }
        DebtSortOrder.AMOUNT_HIGH -> list.sortedByDescending { it.amount }
        DebtSortOrder.REMAINING_HIGH -> list.sortedByDescending { (it.amount - data.collectedAmountForReceivable(it)).coerceAtLeast(0.0) }
      }

      val unpaidPayables = remember(data.payables, data.payablePayments, debtSortOrder) {
        sortPayables(data.payables.filter { (it.amount - data.paidAmountForPayable(it)) > 0.001 })
      }
      val settledPayables = remember(data.payables, data.payablePayments, debtSortOrder) {
        sortPayables(data.payables.filter { (it.amount - data.paidAmountForPayable(it)) <= 0.001 })
      }
      val unpaidReceivables = remember(data.receivables, data.receivablePayments, debtSortOrder) {
        sortReceivables(data.receivables.filter { (it.amount - data.collectedAmountForReceivable(it)) > 0.001 })
      }
      val settledReceivables = remember(data.receivables, data.receivablePayments, debtSortOrder) {
        sortReceivables(data.receivables.filter { (it.amount - data.collectedAmountForReceivable(it)) <= 0.001 })
      }

      val overduePayablesCount = remember(unpaidPayables, nowMillis) {
        unpaidPayables.count { it.dueDateMillis != null && it.dueDateMillis > 0L && it.dueDateMillis < nowMillis }
      }
      val overdueReceivablesCount = remember(unpaidReceivables, nowMillis) {
        unpaidReceivables.count { it.dueDateMillis != null && it.dueDateMillis > 0L && it.dueDateMillis < nowMillis }
      }

      Column(modifier = modifier.fillMaxSize()) {
        if (selectionMode) {
          Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { selectionMode = false; selectedDebtIds = emptySet() }) {
                  Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                }
                Text("${selectedDebtIds.size} ${strings.selectedCountLabel}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
              }
              Button(
                onClick = { confirmBulkDelete = true },
                enabled = selectedDebtIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("bulk_delete_debts_btn"),
              ) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.bulkDeleteButton, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.bulkDeleteButton)
              }
            }
          }
        }

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (!loansOnlyMode) {
            item {
              Text(text = strings.walletsScreenTitle, style = MaterialTheme.typography.headlineMedium)
            }

            // Overall Balance Hero Card
            item {
              val primary = MaterialTheme.colorScheme.primary
              val tertiary = MaterialTheme.colorScheme.tertiary
              Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(colors = listOf(primary, primary.copy(alpha = 0.85f), tertiary.copy(alpha = 0.8f))))
                    .padding(20.dp),
                ) {
                  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.overallWalletBalance, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                    SensitiveAmountText(
                      text = CurrencyFormat.formatSigned(balances.total, currencySymbol),
                      blurBalances = settings.blurBalances,
                      style = MaterialTheme.typography.displayLarge,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                    )
                    if (totalUnpaidPayables > 0.0 || totalUnpaidReceivables > 0.0) {
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (totalUnpaidPayables > 0.0) {
                          Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                              Text(strings.totalPayablesLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                              SensitiveAmountText(
                                text = CurrencyFormat.format(totalUnpaidPayables, currencySymbol),
                                blurBalances = settings.blurBalances,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFE0B2),
                              )
                            }
                          }
                        }
                        if (totalUnpaidReceivables > 0.0) {
                          Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                              Text(strings.totalReceivablesLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                              SensitiveAmountText(
                                text = CurrencyFormat.format(totalUnpaidReceivables, currencySymbol),
                                blurBalances = settings.blurBalances,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBDF2DF),
                              )
                            }
                          }
                        }
                      }
                    }
                    Row(
                      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      QuickWhiteActionChip(Icons.Rounded.Add, strings.balanceTypeTab, onAddBalance, Modifier.testTag("wallet_add_balance_btn"))
                      QuickWhiteActionChip(Icons.Rounded.SwapHoriz, strings.quickTransferAction, onOpenTransfer, Modifier.testTag("wallet_transfer_btn"))
                      QuickWhiteActionChip(Icons.Rounded.Add, strings.quickBorrowAction, onOpenBorrow, Modifier.testTag("wallet_borrow_btn"))
                      QuickWhiteActionChip(Icons.Rounded.NorthEast, strings.quickLendAction, onOpenLend, Modifier.testTag("wallet_lend_btn"))
                    }
                  }
                }
              }
            }

            // Per-Account Balances (Default + Custom Accounts)
            item {
              Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(strings.accountBalancesTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                FilledTonalButton(
                  onClick = onAddAccount,
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("wallets_add_account_btn"),
                ) {
                  Icon(Icons.Rounded.Add, contentDescription = strings.addAccountButton, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text(strings.addAccountButton)
                }
              }
            }

            item {
              FlowRow(
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                accountSummaries.forEach { summary ->
                  AccountSummaryTile(
                    summary = summary,
                    currencySymbol = currencySymbol,
                    blurBalances = settings.blurBalances,
                    onEdit = summary.customAccount?.let { acct -> { onEditAccount(acct) } },
                    onDelete = summary.customAccount?.let { acct -> { accountPendingDelete = acct } },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("account_card_${summary.accountId}"),
                  )
                }
              }
            }
          }

          // Payables vs Receivables Segmented Selector
          item {
            Row(
              modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              FilterChip(
                selected = selectedDebtTab == 0,
                onClick = { selectedDebtTab = 0; selectionMode = false; selectedDebtIds = emptySet() },
                label = { Text("${strings.payablesTab} (${unpaidPayables.size})") },
                modifier = Modifier.testTag("tab_payables_chip"),
              )
              FilterChip(
                selected = selectedDebtTab == 1,
                onClick = { selectedDebtTab = 1; selectionMode = false; selectedDebtIds = emptySet() },
                label = { Text("${strings.receivablesTab} (${unpaidReceivables.size})") },
                modifier = Modifier.testTag("tab_receivables_chip"),
              )
            }
          }

          // Summary KPI Card for the active Debt tab
          item {
            val isPayTab = selectedDebtTab == 0
            val totalAmt = if (isPayTab) data.payables.sumOf { it.amount } else data.receivables.sumOf { it.amount }
            val settledAmt = if (isPayTab) {
              data.payables.sumOf { data.paidAmountForPayable(it) }
            } else {
              data.receivables.sumOf { data.collectedAmountForReceivable(it) }
            }
            val overdueCnt = if (isPayTab) overduePayablesCount else overdueReceivablesCount

            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (isPayTab) strings.totalPayablesLabel else strings.totalReceivablesLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                  )
                  SensitiveAmountText(
                    text = CurrencyFormat.format(totalAmt, currencySymbol),
                    blurBalances = settings.blurBalances,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPayTab) SemanticWarningLight else SemanticSafeLight,
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (isPayTab) strings.paidAmountLabel else strings.collectedAmountLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                  )
                  SensitiveAmountText(
                    text = CurrencyFormat.format(settledAmt, currencySymbol),
                    blurBalances = settings.blurBalances,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(strings.overdueCountLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(
                    text = overdueCnt.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (overdueCnt > 0) SemanticDangerLight else MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }

          // Toolbar Row: Sort, Multi-select, + Record
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = if (selectedDebtTab == 0) strings.unpaidPayablesTitle else strings.unpaidReceivablesTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
              )
              Box {
                IconButton(onClick = { sortMenuExpanded = true }, modifier = Modifier.testTag("debt_sort_btn")) {
                  Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = strings.sortLabel)
                }
                DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                  DropdownMenuItem(text = { Text(strings.sortNewest) }, onClick = { debtSortOrder = DebtSortOrder.NEWEST; sortMenuExpanded = false })
                  DropdownMenuItem(text = { Text(strings.sortOldest) }, onClick = { debtSortOrder = DebtSortOrder.OLDEST; sortMenuExpanded = false })
                  DropdownMenuItem(text = { Text(strings.sortAmountHigh) }, onClick = { debtSortOrder = DebtSortOrder.AMOUNT_HIGH; sortMenuExpanded = false })
                  DropdownMenuItem(text = { Text(strings.dueAmountLabel) }, onClick = { debtSortOrder = DebtSortOrder.REMAINING_HIGH; sortMenuExpanded = false })
                }
              }
              IconButton(
                onClick = {
                  selectionMode = !selectionMode
                  if (!selectionMode) selectedDebtIds = emptySet()
                },
                modifier = Modifier.testTag("debt_multiselect_toggle"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.CheckBox,
                  contentDescription = strings.selectedCountLabel,
                  tint = if (selectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              FilledTonalButton(
                onClick = if (selectedDebtTab == 0) onOpenBorrow else onOpenLend,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (selectedDebtTab == 0) strings.quickBorrowAction else strings.quickLendAction)
              }
            }
          }

          if (selectedDebtTab == 0) {
            if (unpaidPayables.isEmpty()) {
              item { EmptyDebtCard(strings.noPayablesEmpty) }
            } else {
              items(unpaidPayables, key = { "unpaid_${it.id}" }) { payable ->
                val isSel = payable.id in selectedDebtIds
                PayableCard(
                  payable = payable,
                  paidAmount = data.paidAmountForPayable(payable),
                  payments = data.payablePayments.filter { it.payableId == payable.id },
                  accountLabel = data.accountLabel(payable.accountId, payable.paymentMethod, strings),
                  paymentMethodLabelFn = { wire -> data.accountLabel(null, wire, strings) },
                  currencySymbol = currencySymbol,
                  langCode = langCode,
                  showNotes = settings.showNotesInList,
                  blurBalances = settings.blurBalances,
                  nowMillis = nowMillis,
                  strings = strings,
                  selectionMode = selectionMode,
                  selected = isSel,
                  onToggleSelect = { selectedDebtIds = if (isSel) selectedDebtIds - payable.id else selectedDebtIds + payable.id },
                  onRepay = { onRepayPayable(payable) },
                  onEdit = { onEditPayable(payable) },
                  onDelete = { payablePendingDelete = payable },
                  onDeletePayment = { repaymentPendingDelete = it },
                )
              }
            }

            if (settledPayables.isNotEmpty()) {
              item {
                Text(strings.settledPayablesTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
              }
              items(settledPayables, key = { "settled_${it.id}" }) { payable ->
                val isSel = payable.id in selectedDebtIds
                PayableCard(
                  payable = payable,
                  paidAmount = data.paidAmountForPayable(payable),
                  payments = data.payablePayments.filter { it.payableId == payable.id },
                  accountLabel = data.accountLabel(payable.accountId, payable.paymentMethod, strings),
                  paymentMethodLabelFn = { wire -> data.accountLabel(null, wire, strings) },
                  currencySymbol = currencySymbol,
                  langCode = langCode,
                  showNotes = settings.showNotesInList,
                  blurBalances = settings.blurBalances,
                  nowMillis = nowMillis,
                  strings = strings,
                  selectionMode = selectionMode,
                  selected = isSel,
                  onToggleSelect = { selectedDebtIds = if (isSel) selectedDebtIds - payable.id else selectedDebtIds + payable.id },
                  onRepay = null,
                  onEdit = { onEditPayable(payable) },
                  onDelete = { payablePendingDelete = payable },
                  onDeletePayment = { repaymentPendingDelete = it },
                )
              }
            }
          } else {
            if (unpaidReceivables.isEmpty()) {
              item { EmptyDebtCard(strings.noReceivablesEmpty) }
            } else {
              items(unpaidReceivables, key = { "unpaid_rec_${it.id}" }) { rec ->
                val isSel = rec.id in selectedDebtIds
                ReceivableCard(
                  receivable = rec,
                  collectedAmount = data.collectedAmountForReceivable(rec),
                  collections = data.receivablePayments.filter { it.receivableId == rec.id },
                  accountLabel = data.accountLabel(rec.accountId, rec.paymentMethod, strings),
                  paymentMethodLabelFn = { wire -> data.accountLabel(null, wire, strings) },
                  currencySymbol = currencySymbol,
                  langCode = langCode,
                  showNotes = settings.showNotesInList,
                  blurBalances = settings.blurBalances,
                  nowMillis = nowMillis,
                  strings = strings,
                  selectionMode = selectionMode,
                  selected = isSel,
                  onToggleSelect = { selectedDebtIds = if (isSel) selectedDebtIds - rec.id else selectedDebtIds + rec.id },
                  onCollect = { onCollectReceivable(rec) },
                  onEdit = { onEditReceivable(rec) },
                  onDelete = { receivablePendingDelete = rec },
                  onDeleteCollection = { collectionPendingDelete = it },
                )
              }
            }

            if (settledReceivables.isNotEmpty()) {
              item {
                Text(strings.settledReceivablesTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
              }
              items(settledReceivables, key = { "settled_rec_${it.id}" }) { rec ->
                val isSel = rec.id in selectedDebtIds
                ReceivableCard(
                  receivable = rec,
                  collectedAmount = data.collectedAmountForReceivable(rec),
                  collections = data.receivablePayments.filter { it.receivableId == rec.id },
                  accountLabel = data.accountLabel(rec.accountId, rec.paymentMethod, strings),
                  paymentMethodLabelFn = { wire -> data.accountLabel(null, wire, strings) },
                  currencySymbol = currencySymbol,
                  langCode = langCode,
                  showNotes = settings.showNotesInList,
                  blurBalances = settings.blurBalances,
                  nowMillis = nowMillis,
                  strings = strings,
                  selectionMode = selectionMode,
                  selected = isSel,
                  onToggleSelect = { selectedDebtIds = if (isSel) selectedDebtIds - rec.id else selectedDebtIds + rec.id },
                  onCollect = null,
                  onEdit = { onEditReceivable(rec) },
                  onDelete = { receivablePendingDelete = rec },
                  onDeleteCollection = { collectionPendingDelete = it },
                )
              }
            }
          }

          if (!loansOnlyMode && data.transfers.isNotEmpty()) {
            item {
              Text(strings.transferHistoryTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
            }
            items(data.transfers.take(20), key = { "tr_${it.id}" }) { transfer ->
              TransferRowCard(
                transfer = transfer,
                currencySymbol = currencySymbol,
                fromAccountLabel = data.accountLabel(transfer.fromAccountId, transfer.fromMethod, strings),
                toAccountLabel = data.accountLabel(transfer.toAccountId, transfer.toMethod, strings),
                showNotes = settings.showNotesInList,
                blurBalances = settings.blurBalances,
                onClick = { onEditTransfer(transfer) },
                onDelete = { transferPendingDelete = transfer },
              )
            }
          }
        }
      }
    }
  }

  accountPendingDelete?.let { acct ->
    WalletDeleteDialog(
      title = strings.confirmDeleteAccountTitle,
      message = "${acct.name} — ${strings.confirmDeleteAccountMessage}",
      onConfirm = { onDeleteAccount(acct.id); accountPendingDelete = null },
      onDismiss = { accountPendingDelete = null },
    )
  }
  transferPendingDelete?.let { tr ->
    WalletDeleteDialog(
      title = strings.confirmDeleteTransferTitle,
      message = strings.confirmDeleteTransferMessage,
      onConfirm = { onDeleteTransfer(tr); transferPendingDelete = null },
      onDismiss = { transferPendingDelete = null },
    )
  }
  payablePendingDelete?.let { pay ->
    WalletDeleteDialog(
      title = strings.confirmDeletePayableTitle,
      message = "${pay.personName} — ${strings.confirmDeletePayableMessage}",
      onConfirm = { onDeletePayable(pay); payablePendingDelete = null },
      onDismiss = { payablePendingDelete = null },
    )
  }
  repaymentPendingDelete?.let { pmt ->
    WalletDeleteDialog(
      title = strings.confirmDeleteRepaymentTitle,
      message = "${pmt.personName} — ${strings.confirmDeleteRepaymentMessage}",
      onConfirm = { onDeleteRepayment(pmt); repaymentPendingDelete = null },
      onDismiss = { repaymentPendingDelete = null },
    )
  }
  receivablePendingDelete?.let { rec ->
    WalletDeleteDialog(
      title = strings.confirmDeleteReceivableTitle,
      message = "${rec.personName} — ${strings.confirmDeleteReceivableMessage}",
      onConfirm = { onDeleteReceivable(rec); receivablePendingDelete = null },
      onDismiss = { receivablePendingDelete = null },
    )
  }
  collectionPendingDelete?.let { col ->
    WalletDeleteDialog(
      title = strings.confirmDeleteCollectionTitle,
      message = "${col.personName} — ${strings.confirmDeleteCollectionMessage}",
      onConfirm = { onDeleteCollection(col); collectionPendingDelete = null },
      onDismiss = { collectionPendingDelete = null },
    )
  }
  if (confirmBulkDelete) {
    WalletDeleteDialog(
      title = strings.confirmDeleteTitle,
      message = "${selectedDebtIds.size} ${strings.selectedCountLabel}",
      onConfirm = {
        val ids = selectedDebtIds.toList()
        if (selectedDebtTab == 0) onBulkDeletePayables(ids) else onBulkDeleteReceivables(ids)
        selectedDebtIds = emptySet()
        selectionMode = false
        confirmBulkDelete = false
      },
      onDismiss = { confirmBulkDelete = false },
    )
  }
}

@Composable
private fun WalletDeleteDialog(
  title: String,
  message: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val strings = LocalStrings.current
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = { Text(message) },
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
      ) { Text(strings.delete) }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
  )
}

@Composable
private fun EmptyDebtCard(text: String) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SemanticSafeLight)
      Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun QuickWhiteActionChip(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = Color.White.copy(alpha = 0.2f),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
      Spacer(Modifier.width(4.dp))
      Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable
private fun AccountSummaryTile(
  summary: AccountSummary,
  currencySymbol: String,
  blurBalances: Boolean,
  onEdit: (() -> Unit)?,
  onDelete: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val tint = Color(summary.colorHex)
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape = RoundedCornerShape(18.dp),
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .let { if (onEdit != null) it.clickable(onClick = onEdit) else it },
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(CategoryCatalog.iconFor(summary.iconKey), contentDescription = summary.name, tint = tint, modifier = Modifier.size(18.dp))
          }
          Text(
            text = summary.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        if (onEdit != null && onDelete != null) {
          Row {
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
      SensitiveAmountText(
        text = CurrencyFormat.formatSigned(summary.currentBalance, currencySymbol),
        blurBalances = blurBalances,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = if (summary.currentBalance < 0.0) SemanticDangerLight else MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
private fun PayableCard(
  payable: Payable,
  paidAmount: Double,
  payments: List<PayablePayment>,
  accountLabel: String,
  paymentMethodLabelFn: (String) -> String,
  currencySymbol: String,
  langCode: String,
  showNotes: Boolean,
  blurBalances: Boolean,
  nowMillis: Long,
  strings: AppStrings,
  selectionMode: Boolean,
  selected: Boolean,
  onToggleSelect: () -> Unit,
  onRepay: (() -> Unit)?,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onDeletePayment: (PayablePayment) -> Unit,
) {
  val remaining = (payable.amount - paidAmount).coerceAtLeast(0.0)
  val isSettled = remaining <= 0.001
  val isOverdue = !isSettled && payable.dueDateMillis != null && payable.dueDateMillis > 0L && payable.dueDateMillis < nowMillis
  val progress = if (payable.amount > 0.0) (paidAmount / payable.amount).toFloat().coerceIn(0f, 1f) else 1f

  Card(
    colors = CardDefaults.cardColors(
      containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      else MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(18.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable { if (selectionMode) onToggleSelect() else onEdit() },
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (selectionMode) {
          Checkbox(checked = selected, onCheckedChange = { onToggleSelect() })
        }
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(payable.personName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (isOverdue) {
              Surface(color = SemanticDangerLight.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
                Text(strings.overdueBadge, style = MaterialTheme.typography.labelSmall, color = SemanticDangerLight, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }
          }
          Text(
            text = buildString {
              append("$accountLabel • ${DateFormatUtils.formatDateCompact(payable.dateMillis, langCode)}")
              payable.dueDateMillis?.takeIf { it > 0L }?.let { due ->
                append(" • ${strings.dueDateLabel}: ${DateFormatUtils.formatDateCompact(due, langCode)}")
              }
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isOverdue) SemanticDangerLight else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (isSettled) {
          Surface(color = SemanticSafeLight.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
            Text(strings.settledBadge, style = MaterialTheme.typography.labelMedium, color = SemanticSafeLight, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
          }
        } else if (onRepay != null && !selectionMode) {
          FilledTonalButton(onClick = onRepay, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.testTag("repay_btn_${payable.id}")) {
            Text(strings.repayButton)
          }
        }
        if (!selectionMode) {
          IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      LinearProgressIndicator(
        progress = { progress },
        color = if (isSettled) SemanticSafeLight else if (isOverdue) SemanticDangerLight else SemanticWarningLight,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
      )

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SensitiveAmountText(
          text = "${strings.paidAmountLabel}: ${CurrencyFormat.format(paidAmount, currencySymbol)} / ${CurrencyFormat.format(payable.amount, currencySymbol)}",
          blurBalances = blurBalances,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!isSettled) {
          SensitiveAmountText(
            text = "${strings.dueAmountLabel}: ${CurrencyFormat.format(remaining, currencySymbol)}",
            blurBalances = blurBalances,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isOverdue) SemanticDangerLight else SemanticWarningLight,
          )
        }
      }

      if (showNotes && payable.note.isNotBlank()) {
        Text(payable.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      if (payments.isNotEmpty()) {
        Text(strings.repaymentHistoryTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        payments.forEach { pmt ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceContainerLow)
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("${paymentMethodLabelFn(pmt.paymentMethod)} • ${DateFormatUtils.formatDateCompact(pmt.dateMillis, langCode)}", style = MaterialTheme.typography.labelMedium)
              if (showNotes && pmt.note.isNotBlank()) {
                Text(pmt.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
              }
            }
            SensitiveAmountText(
              text = "-${CurrencyFormat.format(pmt.amount, currencySymbol)}",
              blurBalances = blurBalances,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = { onDeletePayment(pmt) }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ReceivableCard(
  receivable: Receivable,
  collectedAmount: Double,
  collections: List<ReceivablePayment>,
  accountLabel: String,
  paymentMethodLabelFn: (String) -> String,
  currencySymbol: String,
  langCode: String,
  showNotes: Boolean,
  blurBalances: Boolean,
  nowMillis: Long,
  strings: AppStrings,
  selectionMode: Boolean,
  selected: Boolean,
  onToggleSelect: () -> Unit,
  onCollect: (() -> Unit)?,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onDeleteCollection: (ReceivablePayment) -> Unit,
) {
  val remaining = (receivable.amount - collectedAmount).coerceAtLeast(0.0)
  val isSettled = remaining <= 0.001
  val isOverdue = !isSettled && receivable.dueDateMillis != null && receivable.dueDateMillis > 0L && receivable.dueDateMillis < nowMillis
  val progress = if (receivable.amount > 0.0) (collectedAmount / receivable.amount).toFloat().coerceIn(0f, 1f) else 1f

  Card(
    colors = CardDefaults.cardColors(
      containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      else MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(18.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable { if (selectionMode) onToggleSelect() else onEdit() },
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (selectionMode) {
          Checkbox(checked = selected, onCheckedChange = { onToggleSelect() })
        }
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(receivable.personName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (isOverdue) {
              Surface(color = SemanticDangerLight.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
                Text(strings.overdueBadge, style = MaterialTheme.typography.labelSmall, color = SemanticDangerLight, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }
          }
          Text(
            text = buildString {
              append("$accountLabel • ${DateFormatUtils.formatDateCompact(receivable.dateMillis, langCode)}")
              receivable.dueDateMillis?.takeIf { it > 0L }?.let { due ->
                append(" • ${strings.dueDateLabel}: ${DateFormatUtils.formatDateCompact(due, langCode)}")
              }
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isOverdue) SemanticDangerLight else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (isSettled) {
          Surface(color = SemanticSafeLight.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
            Text(strings.settledBadge, style = MaterialTheme.typography.labelMedium, color = SemanticSafeLight, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
          }
        } else if (onCollect != null && !selectionMode) {
          FilledTonalButton(onClick = onCollect, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.testTag("collect_btn_${receivable.id}")) {
            Text(strings.collectButton)
          }
        }
        if (!selectionMode) {
          IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      LinearProgressIndicator(
        progress = { progress },
        color = if (isSettled) SemanticSafeLight else if (isOverdue) SemanticDangerLight else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
      )

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SensitiveAmountText(
          text = "${strings.collectedAmountLabel}: ${CurrencyFormat.format(collectedAmount, currencySymbol)} / ${CurrencyFormat.format(receivable.amount, currencySymbol)}",
          blurBalances = blurBalances,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!isSettled) {
          SensitiveAmountText(
            text = "${strings.dueAmountLabel}: ${CurrencyFormat.format(remaining, currencySymbol)}",
            blurBalances = blurBalances,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isOverdue) SemanticDangerLight else MaterialTheme.colorScheme.primary,
          )
        }
      }

      if (showNotes && receivable.note.isNotBlank()) {
        Text(receivable.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      if (collections.isNotEmpty()) {
        Text(strings.collectionHistoryTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        collections.forEach { col ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceContainerLow)
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("${paymentMethodLabelFn(col.paymentMethod)} • ${DateFormatUtils.formatDateCompact(col.dateMillis, langCode)}", style = MaterialTheme.typography.labelMedium)
              if (showNotes && col.note.isNotBlank()) {
                Text(col.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
              }
            }
            SensitiveAmountText(
              text = "+${CurrencyFormat.format(col.amount, currencySymbol)}",
              blurBalances = blurBalances,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = SemanticSafeLight,
            )
            IconButton(onClick = { onDeleteCollection(col) }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}
