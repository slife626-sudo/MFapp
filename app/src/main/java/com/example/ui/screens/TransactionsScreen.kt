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
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SouthWest
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.PeriodFilter
import com.example.data.model.Receivable
import com.example.data.model.ReceivablePayment
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.LocalStrings
import com.example.ui.components.PeriodFilterStrip
import com.example.ui.components.SensitiveAmountText
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.DashboardUiData

enum class TxFilterMode {
  ALL,
  EXPENSE,
  INCOME,
  TRANSFER,
  PAYABLE,
  RECEIVABLE,
}

private enum class TxSortOrder {
  NEWEST,
  OLDEST,
  AMOUNT_HIGH,
  AMOUNT_LOW,
}

private data class UnifiedTxRow(
  val entityType: String,
  val id: String,
  val dateMillis: Long,
  val title: String,
  val subtitle: String,
  val note: String,
  val amount: Double,
  val signedPrefix: String,
  val amountColor: Color,
  val iconKey: String,
  val iconTint: Color,
  val expense: Expense? = null,
  val income: Income? = null,
  val transfer: AccountTransfer? = null,
  val payable: Payable? = null,
  val payablePayment: PayablePayment? = null,
  val receivable: Receivable? = null,
  val receivablePayment: ReceivablePayment? = null,
) {
  val selectionKey: String get() = "$entityType:$id"
}

@Composable
fun TransactionsScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  selectedPeriod: PeriodFilter,
  selectedMonthKey: String,
  onSelectPeriod: (PeriodFilter) -> Unit,
  onShiftMonth: (Int) -> Unit,
  onEditExpense: (Expense) -> Unit,
  onEditIncome: (Income) -> Unit,
  onEditTransfer: (AccountTransfer) -> Unit,
  onEditPayable: (Payable) -> Unit,
  onEditReceivable: (Receivable) -> Unit,
  onDeleteSingle: (entityType: String, id: String) -> Unit,
  onBulkSoftDelete: (List<Pair<String, String>>) -> Unit,
  initialTypeFilter: TxFilterMode? = null,
) {
  val strings = LocalStrings.current
  val symbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  val langCode = settings.language.code

  var searchQuery by remember { mutableStateOf("") }
  var typeFilter by remember(initialTypeFilter) {
    mutableStateOf(initialTypeFilter ?: TxFilterMode.ALL)
  }
  var sortOrder by remember { mutableStateOf(TxSortOrder.NEWEST) }
  var sortMenuExpanded by remember { mutableStateOf(false) }
  var selectionMode by remember { mutableStateOf(false) }
  var selectedKeys by remember { mutableStateOf(emptySet<String>()) }
  var pendingSingleDelete by remember { mutableStateOf<UnifiedTxRow?>(null) }
  var confirmBulkDelete by remember { mutableStateOf(false) }

  val initialPage = if (settings.pageSize <= 0) Int.MAX_VALUE else settings.pageSize
  var displayLimit by remember(settings.pageSize, selectedPeriod, selectedMonthKey, typeFilter, searchQuery) {
    mutableIntStateOf(initialPage)
  }

  LaunchedEffect(initialTypeFilter) {
    if (initialTypeFilter != null) {
      typeFilter = initialTypeFilter
    }
  }

  when (uiState) {
    UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
      Text(uiState.message, color = MaterialTheme.colorScheme.error)
    }
    is UiState.Success -> {
      val data = uiState.data
      val (startMillis, endMillis) = remember(selectedPeriod, selectedMonthKey) {
        DateFormatUtils.periodRangeMillis(selectedPeriod, selectedMonthKey)
      }
      val allCategories = remember(data.customCategories, strings) {
        CategoryCatalog.mergeCategories(strings, data.customCategories, includeIncome = true)
          .associateBy { it.id }
      }

      val primaryColor = MaterialTheme.colorScheme.primary
      val unifiedRows = remember(data, startMillis, endMillis, typeFilter, searchQuery, sortOrder, strings) {
        val raw = buildList {
          if (typeFilter == TxFilterMode.ALL || typeFilter == TxFilterMode.EXPENSE) {
            data.expenses.filter { it.dateMillis in startMillis..endMillis }.forEach { exp ->
              val cat = allCategories[exp.categoryId]
              val acctLbl = data.accountLabel(exp.accountId, exp.paymentMethod, strings)
              add(
                UnifiedTxRow(
                  entityType = "EXPENSE",
                  id = exp.id,
                  dateMillis = exp.dateMillis,
                  title = exp.title,
                  subtitle = "${cat?.name ?: exp.categoryId} • $acctLbl",
                  note = exp.note,
                  amount = exp.amount,
                  signedPrefix = "-",
                  amountColor = SemanticDangerLight,
                  iconKey = cat?.iconKey ?: "other",
                  iconTint = Color(cat?.colorHex ?: 0xFFDC2626),
                  expense = exp,
                )
              )
            }
          }
          if (typeFilter == TxFilterMode.ALL || typeFilter == TxFilterMode.INCOME) {
            data.incomes.filter { it.dateMillis in startMillis..endMillis }.forEach { inc ->
              val cat = inc.categoryId?.let { allCategories[it] }
              val acctLbl = data.accountLabel(inc.accountId, inc.paymentMethod, strings)
              add(
                UnifiedTxRow(
                  entityType = "INCOME",
                  id = inc.id,
                  dateMillis = inc.dateMillis,
                  title = inc.title,
                  subtitle = "${cat?.name ?: strings.entryTypeIncome} • $acctLbl",
                  note = inc.note,
                  amount = inc.amount,
                  signedPrefix = "+",
                  amountColor = SemanticSafeLight,
                  iconKey = cat?.iconKey ?: "salary",
                  iconTint = SemanticSafeLight,
                  income = inc,
                )
              )
            }
          }
          if (typeFilter == TxFilterMode.ALL || typeFilter == TxFilterMode.TRANSFER) {
            data.transfers.filter { it.dateMillis in startMillis..endMillis }.forEach { tr ->
              val fromLbl = data.accountLabel(tr.fromAccountId, tr.fromMethod, strings)
              val toLbl = data.accountLabel(tr.toAccountId, tr.toMethod, strings)
              add(
                UnifiedTxRow(
                  entityType = "TRANSFER",
                  id = tr.id,
                  dateMillis = tr.dateMillis,
                  title = "$fromLbl → $toLbl",
                  subtitle = strings.entryTypeTransfer,
                  note = tr.note,
                  amount = tr.amount,
                  signedPrefix = "",
                  amountColor = primaryColor,
                  iconKey = "exchange",
                  iconTint = primaryColor,
                  transfer = tr,
                )
              )
            }
          }
          if (typeFilter == TxFilterMode.ALL || typeFilter == TxFilterMode.PAYABLE) {
            data.payables.filter { it.dateMillis in startMillis..endMillis }.forEach { pay ->
              val acctLbl = data.accountLabel(pay.accountId, pay.paymentMethod, strings)
              add(
                UnifiedTxRow(
                  entityType = "PAYABLE",
                  id = pay.id,
                  dateMillis = pay.dateMillis,
                  title = pay.personName,
                  subtitle = "${strings.entryTypeBorrow} • $acctLbl",
                  note = pay.note,
                  amount = pay.amount,
                  signedPrefix = "+",
                  amountColor = SemanticWarningLight,
                  iconKey = "wallet",
                  iconTint = SemanticWarningLight,
                  payable = pay,
                )
              )
            }
            data.payablePayments.filter { it.dateMillis in startMillis..endMillis }.forEach { pmt ->
              val acctLbl = data.accountLabel(pmt.accountId, pmt.paymentMethod, strings)
              add(
                UnifiedTxRow(
                  entityType = "PAYABLE_PAYMENT",
                  id = pmt.id,
                  dateMillis = pmt.dateMillis,
                  title = pmt.personName,
                  subtitle = "${strings.entryTypeRepayment} • $acctLbl",
                  note = pmt.note,
                  amount = pmt.amount,
                  signedPrefix = "-",
                  amountColor = SemanticDangerLight,
                  iconKey = "repayment",
                  iconTint = SemanticDangerLight,
                  payablePayment = pmt,
                )
              )
            }
          }
          if (typeFilter == TxFilterMode.ALL || typeFilter == TxFilterMode.RECEIVABLE) {
            data.receivables.filter { it.dateMillis in startMillis..endMillis }.forEach { rec ->
              val acctLbl = data.accountLabel(rec.accountId, rec.paymentMethod, strings)
              add(
                UnifiedTxRow(
                  entityType = "RECEIVABLE",
                  id = rec.id,
                  dateMillis = rec.dateMillis,
                  title = rec.personName,
                  subtitle = "${strings.entryTypeLend} • $acctLbl",
                  note = rec.note,
                  amount = rec.amount,
                  signedPrefix = "-",
                  amountColor = primaryColor,
                  iconKey = "trending",
                  iconTint = primaryColor,
                  receivable = rec,
                )
              )
            }
            data.receivablePayments.filter { it.dateMillis in startMillis..endMillis }.forEach { col ->
              val acctLbl = data.accountLabel(col.accountId, col.paymentMethod, strings)
              add(
                UnifiedTxRow(
                  entityType = "RECEIVABLE_PAYMENT",
                  id = col.id,
                  dateMillis = col.dateMillis,
                  title = col.personName,
                  subtitle = "${strings.entryTypeCollection} • $acctLbl",
                  note = col.note,
                  amount = col.amount,
                  signedPrefix = "+",
                  amountColor = SemanticSafeLight,
                  iconKey = "collection",
                  iconTint = SemanticSafeLight,
                  receivablePayment = col,
                )
              )
            }
          }
        }

        val q = searchQuery.trim().lowercase()
        val filtered = if (q.isEmpty()) raw else raw.filter {
          it.title.lowercase().contains(q) ||
            it.subtitle.lowercase().contains(q) ||
            it.note.lowercase().contains(q)
        }

        when (sortOrder) {
          TxSortOrder.NEWEST -> filtered.sortedByDescending { it.dateMillis }
          TxSortOrder.OLDEST -> filtered.sortedBy { it.dateMillis }
          TxSortOrder.AMOUNT_HIGH -> filtered.sortedByDescending { it.amount }
          TxSortOrder.AMOUNT_LOW -> filtered.sortedBy { it.amount }
        }
      }

      val pagedRows = remember(unifiedRows, displayLimit) {
        if (displayLimit == Int.MAX_VALUE) unifiedRows else unifiedRows.take(displayLimit)
      }
      val hasMore = pagedRows.size < unifiedRows.size

      Column(modifier = Modifier.fillMaxSize()) {
        if (selectionMode) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                  onClick = { selectionMode = false; selectedKeys = emptySet() },
                  modifier = Modifier.testTag("cancel_tx_selection_btn"),
                ) {
                  Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                }
                Text(
                  text = "${selectedKeys.size} ${strings.selectedCountLabel}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              Button(
                onClick = { confirmBulkDelete = true },
                enabled = selectedKeys.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("bulk_delete_tx_btn"),
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
          verticalArrangement = Arrangement.spacedBy(10.dp),
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
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchTransactionsHint) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.searchTransactionsHint) },
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
                  .weight(1f)
                  .testTag("tx_search_input"),
              )
              Box {
                IconButton(
                  onClick = { sortMenuExpanded = true },
                  modifier = Modifier.testTag("tx_sort_button"),
                ) {
                  Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = strings.sortLabel)
                }
                DropdownMenu(
                  expanded = sortMenuExpanded,
                  onDismissRequest = { sortMenuExpanded = false },
                ) {
                  DropdownMenuItem(
                    text = { Text(strings.sortNewest) },
                    onClick = { sortOrder = TxSortOrder.NEWEST; sortMenuExpanded = false },
                  )
                  DropdownMenuItem(
                    text = { Text(strings.sortOldest) },
                    onClick = { sortOrder = TxSortOrder.OLDEST; sortMenuExpanded = false },
                  )
                  DropdownMenuItem(
                    text = { Text(strings.sortAmountHigh) },
                    onClick = { sortOrder = TxSortOrder.AMOUNT_HIGH; sortMenuExpanded = false },
                  )
                  DropdownMenuItem(
                    text = { Text(strings.sortAmountLow) },
                    onClick = { sortOrder = TxSortOrder.AMOUNT_LOW; sortMenuExpanded = false },
                  )
                }
              }
              IconButton(
                onClick = {
                  selectionMode = !selectionMode
                  if (!selectionMode) selectedKeys = emptySet()
                },
                modifier = Modifier.testTag("tx_multiselect_toggle"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.CheckBox,
                  contentDescription = strings.selectedCountLabel,
                  tint = if (selectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          if (initialTypeFilter == null) {
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                val chips = listOf(
                  TxFilterMode.ALL to strings.allFilterTab,
                  TxFilterMode.EXPENSE to strings.expenseTypeTab,
                  TxFilterMode.INCOME to strings.balanceTypeTab,
                  TxFilterMode.TRANSFER to strings.transferTypeTab,
                  TxFilterMode.PAYABLE to strings.borrowTypeTab,
                  TxFilterMode.RECEIVABLE to strings.lendTypeTab,
                )
                chips.forEach { (mode, label) ->
                  FilterChip(
                    selected = typeFilter == mode,
                    onClick = { typeFilter = mode },
                    label = { Text(label) },
                    modifier = Modifier.testTag("tx_filter_${mode.name}"),
                  )
                }
              }
            }
          }

          if (pagedRows.isEmpty()) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth().padding(28.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.ReceiptLong,
                    contentDescription = strings.emptyExpensesTitle,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                  )
                  Text(strings.emptyExpensesTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                  Text(strings.emptyExpensesBody, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
          } else {
            items(pagedRows, key = { it.selectionKey }) { row ->
              val isSelected = row.selectionKey in selectedKeys
              Card(
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                  else MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    if (selectionMode) {
                      selectedKeys = if (isSelected) selectedKeys - row.selectionKey else selectedKeys + row.selectionKey
                    } else {
                      when {
                        row.expense != null -> onEditExpense(row.expense)
                        row.income != null -> onEditIncome(row.income)
                        row.transfer != null -> onEditTransfer(row.transfer)
                        row.payable != null -> onEditPayable(row.payable)
                        row.payablePayment != null -> {
                          data.payables.firstOrNull { it.id == row.payablePayment.payableId }
                            ?.let(onEditPayable)
                        }
                        row.receivable != null -> onEditReceivable(row.receivable)
                        row.receivablePayment != null -> {
                          data.receivables.firstOrNull { it.id == row.receivablePayment.receivableId }
                            ?.let(onEditReceivable)
                        }
                      }
                    }
                  }
                  .testTag("tx_row_${row.selectionKey}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  if (selectionMode) {
                    Checkbox(
                      checked = isSelected,
                      onCheckedChange = {
                        selectedKeys = if (isSelected) selectedKeys - row.selectionKey else selectedKeys + row.selectionKey
                      },
                    )
                  }
                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .clip(CircleShape)
                      .background(row.iconTint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                  ) {
                    val vector = when (row.entityType) {
                      "TRANSFER" -> Icons.Rounded.SwapHoriz
                      "PAYABLE" -> Icons.Rounded.Handshake
                      "PAYABLE_PAYMENT" -> Icons.Rounded.Payments
                      "RECEIVABLE" -> Icons.Rounded.NorthEast
                      "RECEIVABLE_PAYMENT" -> Icons.Rounded.SouthWest
                      "INCOME" -> Icons.Rounded.AccountBalanceWallet
                      else -> CategoryCatalog.iconFor(row.iconKey)
                    }
                    Icon(
                      imageVector = vector,
                      contentDescription = row.title,
                      tint = row.iconTint,
                      modifier = Modifier.size(20.dp),
                    )
                  }
                  Spacer(Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                      text = row.title,
                      style = MaterialTheme.typography.titleMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = "${row.subtitle} • ${DateFormatUtils.formatDateTime(row.dateMillis, langCode, settings.use24HourTime)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    if (settings.showNotesInList && row.note.isNotBlank()) {
                      Text(
                        text = row.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }
                  Spacer(Modifier.width(8.dp))
                  SensitiveAmountText(
                    text = "${row.signedPrefix}${CurrencyFormat.format(row.amount, symbol)}",
                    blurBalances = settings.blurBalances,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = row.amountColor,
                  )
                  if (!selectionMode) {
                    IconButton(
                      onClick = { pendingSingleDelete = row },
                      modifier = Modifier.testTag("delete_tx_${row.selectionKey}"),
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
            }

            if (hasMore) {
              item {
                OutlinedButton(
                  onClick = { displayLimit += settings.pageSize.coerceAtLeast(20) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("tx_load_more_btn"),
                ) {
                  Text("${strings.loadMoreButton} (${unifiedRows.size - pagedRows.size})")
                }
              }
            }
          }
        }
      }
    }
  }

  pendingSingleDelete?.let { row ->
    AlertDialog(
      onDismissRequest = { pendingSingleDelete = null },
      title = { Text(strings.confirmDeleteTitle) },
      text = { Text("${row.title} (${CurrencyFormat.format(row.amount, symbol)})") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteSingle(row.entityType, row.id)
            pendingSingleDelete = null
          },
          modifier = Modifier.testTag("confirm_delete_tx_single"),
        ) {
          Text(strings.delete, color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingSingleDelete = null }) { Text(strings.cancel) }
      },
    )
  }

  if (confirmBulkDelete) {
    AlertDialog(
      onDismissRequest = { confirmBulkDelete = false },
      title = { Text(strings.confirmDeleteTitle) },
      text = { Text("${selectedKeys.size} ${strings.selectedCountLabel}") },
      confirmButton = {
        TextButton(
          onClick = {
            val pairs = selectedKeys.mapNotNull { key ->
              val parts = key.split(":", limit = 2)
              if (parts.size == 2) parts[0] to parts[1] else null
            }
            onBulkSoftDelete(pairs)
            selectedKeys = emptySet()
            selectionMode = false
            confirmBulkDelete = false
          },
          modifier = Modifier.testTag("confirm_bulk_delete_tx"),
        ) {
          Text(strings.delete, color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { confirmBulkDelete = false }) { Text(strings.cancel) }
      },
    )
  }
}
