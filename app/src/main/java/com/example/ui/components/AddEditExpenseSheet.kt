package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountSummary
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryFlowType
import com.example.data.model.CategoryItem
import com.example.data.model.CurrencyFormat
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.Payable
import com.example.data.model.PaymentMethod
import com.example.data.model.Receivable
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily
import com.example.viewmodel.AccountBalances
import com.example.viewmodel.DashboardUiData
import java.util.Calendar

enum class EntrySheetMode {
  EXPENSE,
  INCOME,
  TRANSFER,
  BORROW,
  LEND,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseSheet(
  initialExpense: Expense?,
  categories: List<CategoryItem>,
  currencySymbol: String,
  langCode: String,
  onDismiss: () -> Unit,
  onSaveExpense: (
    title: String,
    amount: Double,
    categoryId: String,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) -> Unit = { _, _, _, _, _, _ -> },
  onCreateCustomCategory: (name: String, iconKey: String, colorHex: Long) -> Unit = { _, _, _ -> },
  initialIncome: Income? = null,
  initialTransfer: AccountTransfer? = null,
  initialPayable: Payable? = null,
  initialReceivable: Receivable? = null,
  initialMode: EntrySheetMode = EntrySheetMode.EXPENSE,
  accountBalances: AccountBalances = AccountBalances(),
  onSaveIncome: (
    title: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) -> Unit = { _, _, _, _, _ -> },
  onSaveTransfer: (
    fromMethod: PaymentMethod,
    toMethod: PaymentMethod,
    amount: Double,
    note: String,
    dateMillis: Long,
  ) -> Unit = { _, _, _, _, _ -> },
  onSavePayable: (
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) -> Unit = { _, _, _, _, _ -> },
  onSaveReceivable: (
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ) -> Unit = { _, _, _, _, _ -> },
  accountSummaries: List<AccountSummary> = emptyList(),
  incomeCategories: List<CategoryItem> = emptyList(),
  use24HourTime: Boolean = false,
  blurBalances: Boolean = false,
  hapticEnabled: Boolean = true,
  onSaveExpenseEx: ((title: String, amount: Double, categoryId: String, paymentMethod: PaymentMethod, note: String, dateMillis: Long, accountId: String?) -> Unit)? = null,
  onSaveIncomeEx: ((title: String, amount: Double, paymentMethod: PaymentMethod, note: String, dateMillis: Long, categoryId: String?, accountId: String?) -> Unit)? = null,
  onSaveTransferEx: ((fromMethod: PaymentMethod, toMethod: PaymentMethod, amount: Double, note: String, dateMillis: Long, fromAccountId: String?, toAccountId: String?) -> Unit)? = null,
  onSavePayableEx: ((personName: String, amount: Double, paymentMethod: PaymentMethod, note: String, dateMillis: Long, dueDateMillis: Long?, accountId: String?) -> Unit)? = null,
  onSaveReceivableEx: ((personName: String, amount: Double, paymentMethod: PaymentMethod, note: String, dateMillis: Long, dueDateMillis: Long?, accountId: String?) -> Unit)? = null,
  onCreateCustomCategoryEx: ((name: String, iconKey: String, colorHex: Long, flowType: CategoryFlowType) -> Unit)? = null,
) {
  val strings = LocalStrings.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val haptic = rememberHapticTrigger(hapticEnabled)

  val isCreatingNew = initialExpense == null &&
    initialIncome == null &&
    initialTransfer == null &&
    initialPayable == null &&
    initialReceivable == null

  var multipleMode by remember { mutableStateOf(false) }
  var batchSavedCount by remember { mutableIntStateOf(0) }

  var mode by remember(initialExpense, initialIncome, initialTransfer, initialPayable, initialReceivable, initialMode) {
    mutableStateOf(
      when {
        initialTransfer != null -> EntrySheetMode.TRANSFER
        initialReceivable != null -> EntrySheetMode.LEND
        initialPayable != null -> EntrySheetMode.BORROW
        initialIncome != null -> EntrySheetMode.INCOME
        initialExpense != null -> EntrySheetMode.EXPENSE
        else -> initialMode
      }
    )
  }

  var title by remember(initialExpense, initialIncome, initialPayable, initialReceivable) {
    mutableStateOf(
      initialExpense?.title
        ?: initialIncome?.title
        ?: initialPayable?.personName
        ?: initialReceivable?.personName
        ?: ""
    )
  }
  var amountText by remember(initialExpense, initialIncome, initialTransfer, initialPayable, initialReceivable) {
    val existingAmount = initialExpense?.amount
      ?: initialIncome?.amount
      ?: initialTransfer?.amount
      ?: initialPayable?.amount
      ?: initialReceivable?.amount
      ?: 0.0
    mutableStateOf(
      if (existingAmount > 0.0) {
        if (existingAmount % 1.0 == 0.0) existingAmount.toLong().toString()
        else existingAmount.toString()
      } else ""
    )
  }
  var selectedCategoryId by remember(initialExpense) {
    mutableStateOf(initialExpense?.categoryId ?: categories.firstOrNull()?.id ?: "food")
  }
  val resolvedIncomeCats = remember(incomeCategories, strings) {
    incomeCategories.ifEmpty { CategoryCatalog.builtInIncomeCategories(strings) }
  }
  var selectedIncomeCategoryId by remember(initialIncome) {
    mutableStateOf(initialIncome?.categoryId ?: resolvedIncomeCats.firstOrNull()?.id ?: "inc_salary")
  }

  var selectedMethod by remember(initialExpense, initialIncome, initialPayable, initialReceivable) {
    mutableStateOf(
      PaymentMethod.fromWire(
        initialExpense?.paymentMethod
          ?: initialIncome?.paymentMethod
          ?: initialPayable?.paymentMethod
          ?: initialReceivable?.paymentMethod
      )
    )
  }
  var selectedAccountKey by remember(initialExpense, initialIncome, initialPayable, initialReceivable, accountSummaries) {
    val acctId = initialExpense?.accountId
      ?: initialIncome?.accountId
      ?: initialPayable?.accountId
      ?: initialReceivable?.accountId
    val key = acctId?.takeIf { id -> accountSummaries.any { it.accountId == id } }
      ?: (DashboardUiData.DEFAULT_ACCOUNT_PREFIX + selectedMethod.wireValue)
    mutableStateOf(key)
  }

  var fromMethod by remember(initialTransfer) {
    mutableStateOf(
      if (initialTransfer != null) PaymentMethod.fromWire(initialTransfer.fromMethod)
      else PaymentMethod.BANK
    )
  }
  var fromAccountKey by remember(initialTransfer, accountSummaries) {
    val key = initialTransfer?.fromAccountId?.takeIf { id -> accountSummaries.any { it.accountId == id } }
      ?: (DashboardUiData.DEFAULT_ACCOUNT_PREFIX + fromMethod.wireValue)
    mutableStateOf(key)
  }
  var toMethod by remember(initialTransfer) {
    mutableStateOf(
      if (initialTransfer != null) PaymentMethod.fromWire(initialTransfer.toMethod)
      else PaymentMethod.CASH
    )
  }
  var toAccountKey by remember(initialTransfer, accountSummaries) {
    val key = initialTransfer?.toAccountId?.takeIf { id -> accountSummaries.any { it.accountId == id } }
      ?: (DashboardUiData.DEFAULT_ACCOUNT_PREFIX + toMethod.wireValue)
    mutableStateOf(key)
  }

  var note by remember(initialExpense, initialIncome, initialTransfer, initialPayable, initialReceivable) {
    mutableStateOf(
      initialExpense?.note
        ?: initialIncome?.note
        ?: initialTransfer?.note
        ?: initialPayable?.note
        ?: initialReceivable?.note
        ?: ""
    )
  }
  var dateMillis by remember(initialExpense, initialIncome, initialTransfer, initialPayable, initialReceivable) {
    mutableLongStateOf(
      initialExpense?.dateMillis
        ?: initialIncome?.dateMillis
        ?: initialTransfer?.dateMillis
        ?: initialPayable?.dateMillis
        ?: initialReceivable?.dateMillis
        ?: System.currentTimeMillis()
    )
  }
  var dueDateMillis by remember(initialPayable, initialReceivable) {
    mutableStateOf(initialPayable?.dueDateMillis ?: initialReceivable?.dueDateMillis)
  }
  var validationError by remember { mutableStateOf<String?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  var showDueDatePicker by remember { mutableStateOf(false) }
  var showCustomCategoryDialog by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = when {
            initialTransfer != null -> strings.editTransfer
            initialReceivable != null -> strings.editLend
            initialPayable != null -> strings.editBorrow
            initialIncome != null -> strings.editBalance
            initialExpense != null -> strings.editExpense
            mode == EntrySheetMode.TRANSFER -> strings.addTransfer
            mode == EntrySheetMode.LEND -> strings.addLend
            mode == EntrySheetMode.BORROW -> strings.addBorrow
            mode == EntrySheetMode.INCOME -> strings.addBalance
            else -> strings.addExpense
          },
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.weight(1f),
        )

        if (isCreatingNew) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
              selected = !multipleMode,
              onClick = { multipleMode = false },
              label = { Text(strings.singleModeTab) },
              modifier = Modifier.testTag("entry_mode_single"),
            )
            FilterChip(
              selected = multipleMode,
              onClick = { multipleMode = true },
              label = { Text(strings.multipleModeTab) },
              modifier = Modifier.testTag("entry_mode_multiple"),
            )
          }
        }
      }

      if (multipleMode && batchSavedCount > 0) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "${strings.savedBadgeCount}: $batchSavedCount • ${strings.addAnotherHint}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }

      if (isCreatingNew) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ModePill(
            selected = mode == EntrySheetMode.EXPENSE,
            icon = Icons.Rounded.ReceiptLong,
            label = strings.expenseTypeTab,
            onClick = { mode = EntrySheetMode.EXPENSE; validationError = null },
            modifier = Modifier.testTag("mode_expense_tab"),
          )
          ModePill(
            selected = mode == EntrySheetMode.INCOME,
            icon = Icons.Rounded.AccountBalanceWallet,
            label = strings.balanceTypeTab,
            onClick = { mode = EntrySheetMode.INCOME; validationError = null },
            modifier = Modifier.testTag("mode_balance_tab"),
          )
          ModePill(
            selected = mode == EntrySheetMode.TRANSFER,
            icon = Icons.Rounded.SwapHoriz,
            label = strings.transferTypeTab,
            onClick = { mode = EntrySheetMode.TRANSFER; validationError = null },
            modifier = Modifier.testTag("mode_transfer_tab"),
          )
          ModePill(
            selected = mode == EntrySheetMode.BORROW,
            icon = Icons.Rounded.Handshake,
            label = strings.borrowTypeTab,
            onClick = { mode = EntrySheetMode.BORROW; validationError = null },
            modifier = Modifier.testTag("mode_borrow_tab"),
          )
          ModePill(
            selected = mode == EntrySheetMode.LEND,
            icon = Icons.Rounded.NorthEast,
            label = strings.lendTypeTab,
            onClick = { mode = EntrySheetMode.LEND; validationError = null },
            modifier = Modifier.testTag("mode_lend_tab"),
          )
        }
      }

      OutlinedTextField(
        value = amountText,
        onValueChange = { input ->
          val filtered = input.filter { it.isDigit() || it == '.' }
          if (filtered.count { it == '.' } <= 1) {
            amountText = filtered
            validationError = null
          }
        },
        label = { Text("${strings.amountLabel} ($currencySymbol)") },
        prefix = {
          Text(
            text = "$currencySymbol ",
            fontFamily = HisabMonoFontFamily,
            fontWeight = FontWeight.Bold,
          )
        },
        textStyle = MaterialTheme.typography.headlineMedium.copy(fontFamily = HisabMonoFontFamily),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("expense_amount_input"),
      )

      if (mode != EntrySheetMode.TRANSFER) {
        OutlinedTextField(
          value = title,
          onValueChange = {
            if (it.length <= 120) {
              title = it
              validationError = null
            }
          },
          label = {
            Text(
              when (mode) {
                EntrySheetMode.INCOME -> strings.balanceTitleLabel
                EntrySheetMode.BORROW -> strings.lenderNameLabel
                EntrySheetMode.LEND -> strings.borrowerNameLabel
                else -> strings.expenseTitleLabel
              }
            )
          },
          placeholder = if (mode == EntrySheetMode.EXPENSE || mode == EntrySheetMode.INCOME) {
            { Text(strings.titleOptionalHint) }
          } else null,
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_title_input"),
        )
      }

      if (mode == EntrySheetMode.BORROW || mode == EntrySheetMode.LEND) {
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = if (mode == EntrySheetMode.LEND) strings.lendAutoReceivableHint else strings.borrowAutoPayableHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(12.dp),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = { showDueDatePicker = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("pick_due_date_button"),
          ) {
            Icon(Icons.Rounded.Event, contentDescription = strings.dueDateOptionalLabel)
            Spacer(Modifier.width(8.dp))
            Text(
              text = dueDateMillis?.let {
                "${strings.dueDateLabel}: ${DateFormatUtils.formatDateCompact(it, langCode)}"
              } ?: strings.dueDateOptionalLabel,
            )
          }
          if (dueDateMillis != null) {
            IconButton(
              onClick = { dueDateMillis = null },
              modifier = Modifier.testTag("clear_due_date_button"),
            ) {
              Icon(Icons.Rounded.Close, contentDescription = strings.clearDueDate)
            }
          }
        }
      }

      if (mode == EntrySheetMode.EXPENSE) {
        Text(
          text = strings.categoryLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          categories.forEach { cat ->
            val selected = selectedCategoryId == cat.id
            FilterChip(
              selected = selected,
              onClick = { selectedCategoryId = cat.id },
              leadingIcon = {
                Icon(
                  imageVector = CategoryCatalog.iconFor(cat.iconKey),
                  contentDescription = cat.name,
                  tint = Color(cat.colorHex),
                  modifier = Modifier.size(18.dp),
                )
              },
              label = { Text(cat.name) },
              modifier = Modifier.testTag("category_chip_${cat.id}"),
            )
          }
          OutlinedButton(
            onClick = { showCustomCategoryDialog = true },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("add_custom_category_button"),
          ) {
            Icon(Icons.Rounded.Add, contentDescription = strings.addCustomCategory, Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(strings.addCustomCategory)
          }
        }
      } else if (mode == EntrySheetMode.INCOME) {
        Text(
          text = strings.categoryLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          resolvedIncomeCats.forEach { cat ->
            val selected = selectedIncomeCategoryId == cat.id
            FilterChip(
              selected = selected,
              onClick = { selectedIncomeCategoryId = cat.id },
              leadingIcon = {
                Icon(
                  imageVector = CategoryCatalog.iconFor(cat.iconKey),
                  contentDescription = cat.name,
                  tint = Color(cat.colorHex),
                  modifier = Modifier.size(18.dp),
                )
              },
              label = { Text(cat.name) },
              modifier = Modifier.testTag("income_category_chip_${cat.id}"),
            )
          }
          OutlinedButton(
            onClick = { showCustomCategoryDialog = true },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("add_custom_income_category_button"),
          ) {
            Icon(Icons.Rounded.Add, contentDescription = strings.addCustomCategory, Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(strings.addCustomCategory)
          }
        }
      }

      if (mode == EntrySheetMode.TRANSFER) {
        Text(
          text = strings.fromAccountLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (accountSummaries.isNotEmpty()) {
          AccountSelectorChips(
            summaries = accountSummaries,
            selectedKey = fromAccountKey,
            currencySymbol = currencySymbol,
            blurBalances = blurBalances,
            onSelect = { acct ->
              fromAccountKey = acct.accountId
              fromMethod = acct.method
              validationError = null
            },
          )
        } else {
          AccountMethodChips(
            selected = fromMethod,
            onSelect = { fromMethod = it; validationError = null },
            accountBalances = accountBalances,
            currencySymbol = currencySymbol,
            strings = strings,
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          IconButton(
            onClick = {
              val tmpM = fromMethod
              fromMethod = toMethod
              toMethod = tmpM
              val tmpKey = fromAccountKey
              fromAccountKey = toAccountKey
              toAccountKey = tmpKey
              validationError = null
            },
            modifier = Modifier.testTag("swap_accounts_btn"),
          ) {
            Icon(
              imageVector = Icons.Rounded.SwapVert,
              contentDescription = "Swap accounts",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }

        Text(
          text = strings.toAccountLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (accountSummaries.isNotEmpty()) {
          AccountSelectorChips(
            summaries = accountSummaries,
            selectedKey = toAccountKey,
            currencySymbol = currencySymbol,
            blurBalances = blurBalances,
            onSelect = { acct ->
              toAccountKey = acct.accountId
              toMethod = acct.method
              validationError = null
            },
          )
        } else {
          AccountMethodChips(
            selected = toMethod,
            onSelect = { toMethod = it; validationError = null },
            accountBalances = accountBalances,
            currencySymbol = currencySymbol,
            strings = strings,
          )
        }
      } else {
        Text(
          text = when (mode) {
            EntrySheetMode.EXPENSE -> strings.selectAccountLabel
            EntrySheetMode.LEND -> strings.lendFromAccountLabel
            else -> strings.receiveMethodLabel
          },
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (accountSummaries.isNotEmpty()) {
          AccountSelectorChips(
            summaries = accountSummaries,
            selectedKey = selectedAccountKey,
            currencySymbol = currencySymbol,
            blurBalances = blurBalances,
            onSelect = { acct ->
              selectedAccountKey = acct.accountId
              selectedMethod = acct.method
            },
          )
        } else {
          AccountMethodChips(
            selected = selectedMethod,
            onSelect = { selectedMethod = it },
            accountBalances = accountBalances,
            currencySymbol = currencySymbol,
            strings = strings,
          )
        }
      }

      // Date + Time row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = { showDatePicker = true },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("entry_date_button"),
        ) {
          Icon(Icons.Rounded.CalendarMonth, contentDescription = strings.dateLabel)
          Spacer(Modifier.width(6.dp))
          Text(DateFormatUtils.formatDateCompact(dateMillis, langCode))
        }
        OutlinedButton(
          onClick = { showTimePicker = true },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("entry_time_button"),
        ) {
          Icon(Icons.Rounded.Schedule, contentDescription = strings.timeLabel)
          Spacer(Modifier.width(6.dp))
          Text(DateFormatUtils.formatTimeOnly(dateMillis, langCode, use24HourTime))
        }
      }

      OutlinedTextField(
        value = note,
        onValueChange = { if (it.length <= 300) note = it },
        label = { Text(strings.noteOptionalLabel) },
        maxLines = 3,
        modifier = Modifier.fillMaxWidth(),
      )

      if (validationError != null) {
        Text(
          text = validationError ?: "",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyMedium,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier
            .weight(1f)
            .height(50.dp),
        ) {
          Text(strings.cancel)
        }
        Button(
          onClick = {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            if (amount <= 0.0 || amount > 1_000_000_000.0) {
              validationError = strings.invalidInputError
              return@Button
            }
            val selectedCustomAccountId = accountSummaries
              .firstOrNull { it.accountId == selectedAccountKey && it.isCustom }?.accountId

            when (mode) {
              EntrySheetMode.TRANSFER -> {
                val fromCustom = accountSummaries.firstOrNull { it.accountId == fromAccountKey && it.isCustom }?.accountId
                val toCustom = accountSummaries.firstOrNull { it.accountId == toAccountKey && it.isCustom }?.accountId
                val distinctCustom = fromCustom != null && toCustom != null && fromCustom != toCustom
                if ((accountSummaries.isNotEmpty() && fromAccountKey == toAccountKey) ||
                  (accountSummaries.isEmpty() && fromMethod == toMethod) ||
                  (fromMethod == toMethod && !distinctCustom && fromAccountKey == toAccountKey)
                ) {
                  validationError = strings.sameAccountTransferError
                  return@Button
                }
                if (fromMethod == toMethod && !(fromCustom != null && toCustom != null && fromCustom != toCustom)) {
                  // Also check one default + one custom of same method: Firestore rule requires distinct fromAccountId & toAccountId if methods match!
                  validationError = strings.sameAccountTransferError
                  return@Button
                }
                haptic()
                if (onSaveTransferEx != null) {
                  onSaveTransferEx(fromMethod, toMethod, amount, note.trim(), dateMillis, fromCustom, toCustom)
                } else {
                  onSaveTransfer(fromMethod, toMethod, amount, note.trim(), dateMillis)
                }
              }
              EntrySheetMode.BORROW -> {
                if (title.trim().isEmpty()) {
                  validationError = strings.invalidInputError
                  return@Button
                }
                haptic()
                if (onSavePayableEx != null) {
                  onSavePayableEx(title.trim(), amount, selectedMethod, note.trim(), dateMillis, dueDateMillis, selectedCustomAccountId)
                } else {
                  onSavePayable(title.trim(), amount, selectedMethod, note.trim(), dateMillis)
                }
              }
              EntrySheetMode.LEND -> {
                if (title.trim().isEmpty()) {
                  validationError = strings.invalidInputError
                  return@Button
                }
                haptic()
                if (onSaveReceivableEx != null) {
                  onSaveReceivableEx(title.trim(), amount, selectedMethod, note.trim(), dateMillis, dueDateMillis, selectedCustomAccountId)
                } else {
                  onSaveReceivable(title.trim(), amount, selectedMethod, note.trim(), dateMillis)
                }
              }
              EntrySheetMode.INCOME -> {
                val resolvedCatName = resolvedIncomeCats.firstOrNull { it.id == selectedIncomeCategoryId }?.name ?: strings.balanceTypeTab
                val finalTitle = title.trim().ifEmpty { resolvedCatName }
                haptic()
                if (onSaveIncomeEx != null) {
                  onSaveIncomeEx(finalTitle, amount, selectedMethod, note.trim(), dateMillis, selectedIncomeCategoryId, selectedCustomAccountId)
                } else {
                  onSaveIncome(finalTitle, amount, selectedMethod, note.trim(), dateMillis)
                }
              }
              EntrySheetMode.EXPENSE -> {
                val resolvedCatName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: strings.catOther
                val finalTitle = title.trim().ifEmpty { resolvedCatName }
                haptic()
                if (onSaveExpenseEx != null) {
                  onSaveExpenseEx(finalTitle, amount, selectedCategoryId, selectedMethod, note.trim(), dateMillis, selectedCustomAccountId)
                } else {
                  onSaveExpense(finalTitle, amount, selectedCategoryId, selectedMethod, note.trim(), dateMillis)
                }
              }
            }

            if (isCreatingNew && multipleMode) {
              batchSavedCount++
              amountText = ""
              title = ""
              note = ""
              dueDateMillis = null
            } else {
              onDismiss()
            }
          },
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("save_expense_button"),
        ) {
          Text(strings.save)
        }
      }
    }
  }

  if (showDatePicker) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            pickerState.selectedDateMillis?.let { pickedDate ->
              val oldCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
              val newCal = Calendar.getInstance().apply {
                timeInMillis = pickedDate
                set(Calendar.HOUR_OF_DAY, oldCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, oldCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
              }
              dateMillis = newCal.timeInMillis
            }
            showDatePicker = false
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text(strings.cancel)
        }
      },
    ) {
      DatePicker(state = pickerState)
    }
  }

  if (showTimePicker) {
    val curCal = remember(dateMillis) { Calendar.getInstance().apply { timeInMillis = dateMillis } }
    val timeState = rememberTimePickerState(
      initialHour = curCal.get(Calendar.HOUR_OF_DAY),
      initialMinute = curCal.get(Calendar.MINUTE),
      is24Hour = use24HourTime,
    )
    AlertDialog(
      onDismissRequest = { showTimePicker = false },
      title = { Text(strings.timeLabel) },
      text = { TimePicker(state = timeState) },
      confirmButton = {
        TextButton(
          onClick = {
            val updated = Calendar.getInstance().apply {
              timeInMillis = dateMillis
              set(Calendar.HOUR_OF_DAY, timeState.hour)
              set(Calendar.MINUTE, timeState.minute)
              set(Calendar.SECOND, 0)
            }
            dateMillis = updated.timeInMillis
            showTimePicker = false
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showTimePicker = false }) {
          Text(strings.cancel)
        }
      },
    )
  }

  if (showDueDatePicker) {
    val pickerState = rememberDatePickerState(
      initialSelectedDateMillis = dueDateMillis ?: (System.currentTimeMillis() + 7L * 86_400_000L),
    )
    DatePickerDialog(
      onDismissRequest = { showDueDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            pickerState.selectedDateMillis?.let { dueDateMillis = it }
            showDueDatePicker = false
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDueDatePicker = false }) {
          Text(strings.cancel)
        }
      },
    ) {
      DatePicker(state = pickerState)
    }
  }

  if (showCustomCategoryDialog) {
    AddCustomCategoryDialog(
      initialFlowType = if (mode == EntrySheetMode.INCOME) CategoryFlowType.INCOME else CategoryFlowType.EXPENSE,
      langCode = langCode,
      onDismiss = { showCustomCategoryDialog = false },
      onConfirm = { name, iconKey, colorHex ->
        onCreateCustomCategory(name, iconKey, colorHex)
        showCustomCategoryDialog = false
      },
      onConfirmWithType = onCreateCustomCategoryEx?.let { cb ->
        { name, iconKey, colorHex, flowType ->
          cb(name, iconKey, colorHex, flowType)
          showCustomCategoryDialog = false
        }
      },
    )
  }
}

@Composable
fun AddCustomCategoryDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, iconKey: String, colorHex: Long) -> Unit = { _, _, _ -> },
  initialName: String = "",
  initialIconKey: String = "pets",
  initialColorHex: Long = CategoryCatalog.colorOptions.first(),
  initialFlowType: CategoryFlowType = CategoryFlowType.EXPENSE,
  langCode: String = "bn",
  isEditing: Boolean = false,
  onConfirmWithType: ((name: String, iconKey: String, colorHex: Long, flowType: CategoryFlowType) -> Unit)? = null,
) {
  val strings = LocalStrings.current
  var name by remember(initialName) { mutableStateOf(initialName) }
  var selectedIcon by remember(initialIconKey) { mutableStateOf(initialIconKey) }
  var selectedColor by remember(initialColorHex) { mutableLongStateOf(initialColorHex) }
  var selectedType by remember(initialFlowType) { mutableStateOf(initialFlowType) }
  var showIconPicker by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.addCustomCategory) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
      ) {
        if (onConfirmWithType != null) {
          Text(strings.categoryTypeLabel, style = MaterialTheme.typography.labelMedium)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = selectedType == CategoryFlowType.EXPENSE,
              onClick = { selectedType = CategoryFlowType.EXPENSE },
              label = { Text(strings.expenseCategoryType) },
            )
            FilterChip(
              selected = selectedType == CategoryFlowType.INCOME,
              onClick = { selectedType = CategoryFlowType.INCOME },
              label = { Text(strings.incomeCategoryType) },
            )
          }
        }

        OutlinedTextField(
          value = name,
          onValueChange = { if (it.length <= 40) name = it },
          label = { Text(strings.customCategoryName) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_category_name_input"),
        )

        Surface(
          color = MaterialTheme.colorScheme.surfaceContainerLow,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { showIconPicker = true }
            .testTag("category_icon_picker_button"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(selectedColor).copy(alpha = 0.16f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = CategoryCatalog.iconFor(selectedIcon),
                contentDescription = strings.chooseIcon,
                tint = Color(selectedColor),
              )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(strings.chooseIcon, style = MaterialTheme.typography.labelLarge)
              Text(selectedIcon, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.Edit, contentDescription = strings.chooseIcon)
          }
        }

        Text(strings.chooseColor, style = MaterialTheme.typography.labelMedium)
        ColorSwatchPicker(
          selectedColorHex = selectedColor,
          onSelectColor = { selectedColor = it },
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val clean = name.trim()
          if (clean.isNotEmpty()) {
            if (onConfirmWithType != null) {
              onConfirmWithType(clean, selectedIcon, selectedColor, selectedType)
            } else {
              onConfirm(clean, selectedIcon, selectedColor)
            }
          }
        },
        enabled = name.trim().isNotEmpty(),
        modifier = Modifier.testTag("save_custom_category_button"),
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    },
  )

  if (showIconPicker) {
    IconPickerDialog(
      selectedIconKey = selectedIcon,
      langCode = langCode,
      onDismiss = { showIconPicker = false },
      onSelectIcon = { selectedIcon = it },
    )
  }
}

@Composable
private fun ModePill(
  selected: Boolean,
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
    shape = RoundedCornerShape(14.dp),
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountMethodChips(
  selected: PaymentMethod,
  onSelect: (PaymentMethod) -> Unit,
  accountBalances: AccountBalances,
  currencySymbol: String,
  strings: AppStrings,
) {
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    PaymentMethod.entries.forEach { method ->
      val label = when (method) {
        PaymentMethod.CASH -> strings.cash
        PaymentMethod.CARD -> strings.card
        PaymentMethod.MOBILE -> strings.mobileBanking
        PaymentMethod.BANK -> strings.bank
      }
      val bal = accountBalances.forMethod(method)
      val balStr = CurrencyFormat.formatSigned(bal, currencySymbol)
      FilterChip(
        selected = selected == method,
        onClick = { onSelect(method) },
        label = { Text("$label ($balStr)") },
      )
    }
  }
}
