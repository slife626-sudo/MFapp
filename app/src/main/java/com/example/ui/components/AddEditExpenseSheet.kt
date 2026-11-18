package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.ReceiptLong
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryItem
import com.example.data.model.CurrencyFormat
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.Payable
import com.example.data.model.PaymentMethod
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily
import com.example.viewmodel.AccountBalances

enum class EntrySheetMode {
  EXPENSE,
  INCOME,
  TRANSFER,
  BORROW,
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
  ) -> Unit,
  onCreateCustomCategory: (name: String, iconKey: String, colorHex: Long) -> Unit,
  initialIncome: Income? = null,
  initialTransfer: AccountTransfer? = null,
  initialPayable: Payable? = null,
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
) {
  val strings = LocalStrings.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val isCreatingNew = initialExpense == null &&
    initialIncome == null &&
    initialTransfer == null &&
    initialPayable == null

  var mode by remember(initialExpense, initialIncome, initialTransfer, initialPayable, initialMode) {
    mutableStateOf(
      when {
        initialTransfer != null -> EntrySheetMode.TRANSFER
        initialPayable != null -> EntrySheetMode.BORROW
        initialIncome != null -> EntrySheetMode.INCOME
        initialExpense != null -> EntrySheetMode.EXPENSE
        else -> initialMode
      }
    )
  }

  var title by remember(initialExpense, initialIncome, initialPayable) {
    mutableStateOf(
      initialExpense?.title
        ?: initialIncome?.title
        ?: initialPayable?.personName
        ?: ""
    )
  }
  var amountText by remember(initialExpense, initialIncome, initialTransfer, initialPayable) {
    val existingAmount = initialExpense?.amount
      ?: initialIncome?.amount
      ?: initialTransfer?.amount
      ?: initialPayable?.amount
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
  var selectedMethod by remember(initialExpense, initialIncome, initialPayable) {
    mutableStateOf(
      PaymentMethod.fromWire(
        initialExpense?.paymentMethod
          ?: initialIncome?.paymentMethod
          ?: initialPayable?.paymentMethod
      )
    )
  }
  var fromMethod by remember(initialTransfer) {
    mutableStateOf(
      if (initialTransfer != null) PaymentMethod.fromWire(initialTransfer.fromMethod)
      else PaymentMethod.BANK
    )
  }
  var toMethod by remember(initialTransfer) {
    mutableStateOf(
      if (initialTransfer != null) PaymentMethod.fromWire(initialTransfer.toMethod)
      else PaymentMethod.CASH
    )
  }
  var note by remember(initialExpense, initialIncome, initialTransfer, initialPayable) {
    mutableStateOf(
      initialExpense?.note
        ?: initialIncome?.note
        ?: initialTransfer?.note
        ?: initialPayable?.note
        ?: ""
    )
  }
  var dateMillis by remember(initialExpense, initialIncome, initialTransfer, initialPayable) {
    mutableLongStateOf(
      initialExpense?.dateMillis
        ?: initialIncome?.dateMillis
        ?: initialTransfer?.dateMillis
        ?: initialPayable?.dateMillis
        ?: System.currentTimeMillis()
    )
  }
  var validationError by remember { mutableStateOf<String?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }
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
      Text(
        text = when {
          initialTransfer != null -> strings.editTransfer
          initialPayable != null -> strings.editBorrow
          initialIncome != null -> strings.editBalance
          initialExpense != null -> strings.editExpense
          mode == EntrySheetMode.TRANSFER -> strings.addTransfer
          mode == EntrySheetMode.BORROW -> strings.addBorrow
          mode == EntrySheetMode.INCOME -> strings.addBalance
          else -> strings.addExpense
        },
        style = MaterialTheme.typography.titleLarge,
      )

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
                else -> strings.expenseTitleLabel
              }
            )
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_title_input"),
        )
      }

      if (mode == EntrySheetMode.BORROW) {
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = strings.borrowAutoPayableHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(12.dp),
          )
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
      }

      if (mode == EntrySheetMode.TRANSFER) {
        Text(
          text = strings.fromAccountLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AccountMethodChips(
          selected = fromMethod,
          onSelect = { fromMethod = it; validationError = null },
          accountBalances = accountBalances,
          currencySymbol = currencySymbol,
          strings = strings,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          IconButton(
            onClick = {
              val tmp = fromMethod
              fromMethod = toMethod
              toMethod = tmp
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
        AccountMethodChips(
          selected = toMethod,
          onSelect = { toMethod = it; validationError = null },
          accountBalances = accountBalances,
          currencySymbol = currencySymbol,
          strings = strings,
        )
      } else {
        Text(
          text = if (mode == EntrySheetMode.EXPENSE) strings.paymentMethodLabel else strings.receiveMethodLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AccountMethodChips(
          selected = selectedMethod,
          onSelect = { selectedMethod = it },
          accountBalances = accountBalances,
          currencySymbol = currencySymbol,
          strings = strings,
        )
      }

      // Date button
      OutlinedButton(
        onClick = { showDatePicker = true },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Rounded.CalendarMonth, contentDescription = strings.dateLabel)
        Spacer(Modifier.width(8.dp))
        Text("${strings.dateLabel}: ${DateFormatUtils.formatDateCompact(dateMillis, langCode)}")
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
            when (mode) {
              EntrySheetMode.TRANSFER -> {
                if (fromMethod == toMethod) {
                  validationError = strings.sameAccountTransferError
                  return@Button
                }
                onSaveTransfer(fromMethod, toMethod, amount, note.trim(), dateMillis)
              }
              EntrySheetMode.BORROW -> {
                if (title.trim().isEmpty()) {
                  validationError = strings.invalidInputError
                  return@Button
                }
                onSavePayable(title.trim(), amount, selectedMethod, note.trim(), dateMillis)
              }
              EntrySheetMode.INCOME -> {
                if (title.trim().isEmpty()) {
                  validationError = strings.invalidInputError
                  return@Button
                }
                onSaveIncome(title.trim(), amount, selectedMethod, note.trim(), dateMillis)
              }
              EntrySheetMode.EXPENSE -> {
                if (title.trim().isEmpty()) {
                  validationError = strings.invalidInputError
                  return@Button
                }
                onSaveExpense(
                  title.trim(),
                  amount,
                  selectedCategoryId,
                  selectedMethod,
                  note.trim(),
                  dateMillis,
                )
              }
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
            pickerState.selectedDateMillis?.let { dateMillis = it }
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

  if (showCustomCategoryDialog) {
    AddCustomCategoryDialog(
      onDismiss = { showCustomCategoryDialog = false },
      onConfirm = { name, iconKey, colorHex ->
        onCreateCustomCategory(name, iconKey, colorHex)
        showCustomCategoryDialog = false
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCustomCategoryDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, iconKey: String, colorHex: Long) -> Unit,
) {
  val strings = LocalStrings.current
  var name by remember { mutableStateOf("") }
  var selectedIcon by remember { mutableStateOf("pets") }
  var selectedColor by remember { mutableLongStateOf(CategoryCatalog.colorOptions.first()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.addCustomCategory) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { if (it.length <= 40) name = it },
          label = { Text(strings.customCategoryName) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Text(strings.chooseColor, style = MaterialTheme.typography.labelMedium)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CategoryCatalog.colorOptions.forEach { hex ->
            val isSelected = selectedColor == hex
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(hex))
                .border(
                  width = if (isSelected) 3.dp else 0.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                  shape = CircleShape,
                )
                .clickable { selectedColor = hex },
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(
                  Icons.Rounded.Check,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
        Text(strings.chooseIcon, style = MaterialTheme.typography.labelMedium)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CategoryCatalog.iconOptions.forEach { (key, vector) ->
            val isSelected = selectedIcon == key
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceContainerLow,
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedIcon = key },
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = vector,
                  contentDescription = key,
                  tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val clean = name.trim()
          if (clean.isNotEmpty()) {
            onConfirm(clean, selectedIcon, selectedColor)
          }
        },
        enabled = name.trim().isNotEmpty(),
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    },
  )
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
      val balStr = if (bal < 0.0) {
        "-${CurrencyFormat.format(-bal, currencySymbol)}"
      } else {
        CurrencyFormat.format(bal, currencySymbol)
      }
      FilterChip(
        selected = selected == method,
        onClick = { onSelect(method) },
        label = { Text("$label ($balStr)") },
      )
    }
  }
}
