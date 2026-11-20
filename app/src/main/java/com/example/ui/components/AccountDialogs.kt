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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountSummary
import com.example.data.model.CategoryCatalog
import com.example.data.model.CurrencyFormat
import com.example.data.model.CustomAccount
import com.example.data.model.PaymentMethod
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAccountSheet(
  initialAccount: CustomAccount?,
  currencySymbol: String,
  langCode: String,
  onDismiss: () -> Unit,
  onSave: (
    name: String,
    accountType: PaymentMethod,
    iconKey: String,
    colorHex: Long,
    openingBalance: Double,
  ) -> Unit,
  onDelete: (() -> Unit)? = null,
) {
  val strings = LocalStrings.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var name by remember(initialAccount) { mutableStateOf(initialAccount?.name.orEmpty()) }
  var accountType by remember(initialAccount) {
    mutableStateOf(
      initialAccount?.let { PaymentMethod.fromWire(it.accountType) } ?: PaymentMethod.BANK
    )
  }
  var iconKey by remember(initialAccount) { mutableStateOf(initialAccount?.iconKey ?: "bank") }
  var colorHex by remember(initialAccount) {
    mutableLongStateOf(initialAccount?.colorHex ?: 0xFF2563EB)
  }
  var openingBalanceText by remember(initialAccount) {
    val bal = initialAccount?.openingBalance ?: 0.0
    mutableStateOf(
      if (bal == 0.0) "" else if (bal % 1.0 == 0.0) bal.toLong().toString() else bal.toString()
    )
  }
  var validationError by remember { mutableStateOf<String?>(null) }
  var showIconPicker by remember { mutableStateOf(false) }

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
          text = if (initialAccount != null) strings.editAccountTitle else strings.addAccountButton,
          style = MaterialTheme.typography.titleLarge,
        )
        if (initialAccount != null && onDelete != null) {
          IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_account_sheet_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.DeleteOutline,
              contentDescription = strings.delete,
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      if (initialAccount == null) {
        Text(
          text = strings.presetsLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CategoryCatalog.accountPresets.forEach { preset ->
            val presetLabel = if (langCode == "bn") preset.nameBn else preset.nameEn
            FilterChip(
              selected = name == presetLabel && accountType == preset.method,
              onClick = {
                name = presetLabel
                accountType = preset.method
                iconKey = preset.iconKey
                colorHex = preset.colorHex
                validationError = null
              },
              leadingIcon = {
                Icon(
                  imageVector = CategoryCatalog.iconFor(preset.iconKey),
                  contentDescription = null,
                  tint = Color(preset.colorHex),
                  modifier = Modifier.size(16.dp),
                )
              },
              label = { Text(presetLabel) },
              modifier = Modifier.testTag("account_preset_${preset.id}"),
            )
          }
        }
      }

      OutlinedTextField(
        value = name,
        onValueChange = {
          if (it.length <= 60) {
            name = it
            validationError = null
          }
        },
        label = { Text(strings.accountNameLabel) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("account_name_input"),
      )

      Text(
        text = strings.accountTypeLabel,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        PaymentMethod.entries.forEach { method ->
          FilterChip(
            selected = accountType == method,
            onClick = { accountType = method },
            label = { Text(methodLabel(method, strings)) },
            modifier = Modifier.testTag("account_type_${method.wireValue}"),
          )
        }
      }

      OutlinedTextField(
        value = openingBalanceText,
        onValueChange = { raw ->
          val filtered = raw.filter { it.isDigit() || it == '.' || it == '-' }
          if (filtered.count { it == '.' } <= 1 && filtered.count { it == '-' } <= 1) {
            openingBalanceText = filtered
          }
        },
        label = { Text("${strings.openingBalanceLabel} ($currencySymbol)") },
        textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = HisabMonoFontFamily),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("account_opening_balance_input"),
      )

      Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .clickable { showIconPicker = true }
          .testTag("account_icon_picker_button"),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color(colorHex).copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = CategoryCatalog.iconFor(iconKey),
              contentDescription = strings.chooseIcon,
              tint = Color(colorHex),
            )
          }
          Spacer(Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(strings.chooseIcon, style = MaterialTheme.typography.labelLarge)
            Text(
              text = iconKey,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Icon(Icons.Rounded.Edit, contentDescription = strings.chooseIcon)
        }
      }

      Text(
        text = strings.chooseColor,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      ColorSwatchPicker(
        selectedColorHex = colorHex,
        onSelectColor = { colorHex = it },
      )

      if (validationError != null) {
        Text(
          text = validationError ?: "",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
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
            .height(48.dp),
        ) {
          Text(strings.cancel)
        }
        Button(
          onClick = {
            val cleanName = name.trim()
            if (cleanName.isEmpty()) {
              validationError = strings.invalidInputError
              return@Button
            }
            val opening = openingBalanceText.toDoubleOrNull() ?: 0.0
            onSave(cleanName, accountType, iconKey, colorHex, opening)
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("save_account_button"),
        ) {
          Text(strings.save)
        }
      }
    }
  }

  if (showIconPicker) {
    IconPickerDialog(
      selectedIconKey = iconKey,
      langCode = langCode,
      onDismiss = { showIconPicker = false },
      onSelectIcon = { iconKey = it },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountSelectorChips(
  summaries: List<AccountSummary>,
  selectedKey: String,
  currencySymbol: String,
  blurBalances: Boolean = false,
  onSelect: (AccountSummary) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    summaries.forEach { summary ->
      val selected = summary.accountId == selectedKey
      val balStr = CurrencyFormat.formatSigned(summary.currentBalance, currencySymbol)
      val displayBal = if (blurBalances) "••••" else balStr
      FilterChip(
        selected = selected,
        onClick = { onSelect(summary) },
        leadingIcon = {
          Icon(
            imageVector = CategoryCatalog.iconFor(summary.iconKey),
            contentDescription = summary.name,
            tint = Color(summary.colorHex),
            modifier = Modifier.size(16.dp),
          )
        },
        label = {
          Text(
            text = "${summary.name} ($displayBal)",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        modifier = Modifier.testTag("select_acct_${summary.accountId}"),
      )
    }
  }
}

private fun methodLabel(method: PaymentMethod, strings: AppStrings): String = when (method) {
  PaymentMethod.CASH -> strings.cash
  PaymentMethod.CARD -> strings.card
  PaymentMethod.MOBILE -> strings.mobileBanking
  PaymentMethod.BANK -> strings.bank
}
