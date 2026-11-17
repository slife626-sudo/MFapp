package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryItem
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.PaymentMethod
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily

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
) {
  val strings = LocalStrings.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var title by remember(initialExpense) { mutableStateOf(initialExpense?.title ?: "") }
  var amountText by remember(initialExpense) {
    mutableStateOf(
      if (initialExpense != null && initialExpense.amount > 0.0) {
        if (initialExpense.amount % 1.0 == 0.0) initialExpense.amount.toLong().toString()
        else initialExpense.amount.toString()
      } else ""
    )
  }
  var selectedCategoryId by remember(initialExpense) {
    mutableStateOf(initialExpense?.categoryId ?: categories.firstOrNull()?.id ?: "food")
  }
  var selectedMethod by remember(initialExpense) {
    mutableStateOf(PaymentMethod.fromWire(initialExpense?.paymentMethod))
  }
  var note by remember(initialExpense) { mutableStateOf(initialExpense?.note ?: "") }
  var dateMillis by remember(initialExpense) {
    mutableLongStateOf(initialExpense?.dateMillis ?: System.currentTimeMillis())
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
        text = if (initialExpense == null) strings.addExpense else strings.editExpense,
        style = MaterialTheme.typography.titleLarge,
      )

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

      OutlinedTextField(
        value = title,
        onValueChange = {
          if (it.length <= 120) {
            title = it
            validationError = null
          }
        },
        label = { Text(strings.expenseTitleLabel) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("expense_title_input"),
      )

      // Category selection
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

      // Payment method
      Text(
        text = strings.paymentMethodLabel,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        PaymentMethod.entries.forEach { method ->
          val label = when (method) {
            PaymentMethod.CASH -> strings.cash
            PaymentMethod.CARD -> strings.card
            PaymentMethod.MOBILE -> strings.mobileBanking
            PaymentMethod.BANK -> strings.bank
          }
          FilterChip(
            selected = selectedMethod == method,
            onClick = { selectedMethod = method },
            label = { Text(label) },
          )
        }
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
            if (title.trim().isEmpty() || amount <= 0.0 || amount > 1_000_000_000.0) {
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
