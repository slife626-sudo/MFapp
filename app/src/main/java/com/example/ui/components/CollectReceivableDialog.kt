package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrencyFormat
import com.example.data.model.DateFormatUtils
import com.example.data.model.PaymentMethod
import com.example.data.model.Receivable
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily
import com.example.viewmodel.AccountBalances

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CollectReceivableDialog(
  receivable: Receivable,
  collectedAlready: Double,
  accountBalances: AccountBalances,
  currencySymbol: String,
  langCode: String,
  onDismiss: () -> Unit,
  onConfirm: (amount: Double, paymentMethod: PaymentMethod, note: String, dateMillis: Long) -> Unit,
) {
  val strings = LocalStrings.current
  val remainingDue = (receivable.amount - collectedAlready).coerceAtLeast(0.0)

  var amountText by remember(receivable.id, remainingDue) {
    val defaultAmt = if (remainingDue > 0.0) remainingDue else receivable.amount
    mutableStateOf(
      if (defaultAmt % 1.0 == 0.0) defaultAmt.toLong().toString() else defaultAmt.toString()
    )
  }
  var selectedMethod by remember(receivable.id) {
    mutableStateOf(PaymentMethod.fromWire(receivable.paymentMethod))
  }
  var note by remember { mutableStateOf("") }
  var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
  var showDatePicker by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.collectReceivableTitle) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = receivable.personName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = "${strings.dueAmountLabel}: ${CurrencyFormat.format(remainingDue, currencySymbol)}",
              fontFamily = HisabMonoFontFamily,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
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
          textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = HisabMonoFontFamily),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("collect_amount_input"),
        )

        Text(
          text = strings.collectedToAccountLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          PaymentMethod.entries.forEach { method ->
            val methodLabel = when (method) {
              PaymentMethod.CASH -> strings.cash
              PaymentMethod.CARD -> strings.card
              PaymentMethod.MOBILE -> strings.mobileBanking
              PaymentMethod.BANK -> strings.bank
            }
            val bal = accountBalances.forMethod(method)
            FilterChip(
              selected = selectedMethod == method,
              onClick = { selectedMethod = method },
              label = { Text("$methodLabel (${CurrencyFormat.format(bal, currencySymbol)})") },
            )
          }
        }

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
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        if (validationError != null) {
          Text(
            text = validationError ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amount = amountText.toDoubleOrNull() ?: 0.0
          if (amount <= 0.0 || amount > 1_000_000_000.0) {
            validationError = strings.invalidInputError
            return@Button
          }
          onConfirm(amount, selectedMethod, note.trim(), dateMillis)
        },
        modifier = Modifier.testTag("confirm_collect_button"),
      ) {
        Text(strings.collectButton)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    },
  )

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
}
