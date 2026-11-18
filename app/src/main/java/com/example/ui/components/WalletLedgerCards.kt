package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountTransfer
import com.example.data.model.CurrencyFormat
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.PaymentMethod
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.AccountBalances

@Composable
fun AccountWalletsStrip(
  balances: AccountBalances,
  totalUnpaidPayables: Double,
  currencySymbol: String,
  onClickAccounts: () -> Unit,
  onAddBalance: () -> Unit,
  onOpenTransfer: () -> Unit,
  onOpenBorrow: () -> Unit,
) {
  val strings = LocalStrings.current

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AccountMiniPill(
        label = strings.cash,
        amount = balances.cash,
        currencySymbol = currencySymbol,
        icon = Icons.Rounded.Payments,
        tint = SemanticSafeLight,
        onClick = onClickAccounts,
        modifier = Modifier.testTag("acct_pill_cash"),
      )
      AccountMiniPill(
        label = strings.bank,
        amount = balances.bank,
        currencySymbol = currencySymbol,
        icon = Icons.Rounded.AccountBalance,
        tint = MaterialTheme.colorScheme.primary,
        onClick = onClickAccounts,
        modifier = Modifier.testTag("acct_pill_bank"),
      )
      AccountMiniPill(
        label = strings.mobileBanking,
        amount = balances.mobile,
        currencySymbol = currencySymbol,
        icon = Icons.Rounded.PhoneAndroid,
        tint = MaterialTheme.colorScheme.tertiary,
        onClick = onClickAccounts,
        modifier = Modifier.testTag("acct_pill_mobile"),
      )
      AccountMiniPill(
        label = strings.card,
        amount = balances.card,
        currencySymbol = currencySymbol,
        icon = Icons.Rounded.CreditCard,
        tint = MaterialTheme.colorScheme.secondary,
        onClick = onClickAccounts,
        modifier = Modifier.testTag("acct_pill_card"),
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      OutlinedButton(
        onClick = onAddBalance,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f).testTag("quick_add_balance_btn"),
      ) {
        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(strings.balanceTypeTab, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
      OutlinedButton(
        onClick = onOpenTransfer,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f).testTag("quick_transfer_btn"),
      ) {
        Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(strings.quickTransferAction, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
      OutlinedButton(
        onClick = onOpenBorrow,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f).testTag("quick_borrow_btn"),
      ) {
        Icon(Icons.Rounded.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
          text = if (totalUnpaidPayables > 0.0) {
            "${strings.quickBorrowAction}"
          } else {
            strings.quickBorrowAction
          },
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun AccountMiniPill(
  label: String,
  amount: Double,
  currencySymbol: String,
  icon: ImageVector,
  tint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    shape = RoundedCornerShape(14.dp),
    tonalElevation = 1.dp,
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
      }
      Column {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
        val amountStr = if (amount < 0.0) {
          "-${CurrencyFormat.format(-amount, currencySymbol)}"
        } else {
          CurrencyFormat.format(amount, currencySymbol)
        }
        Text(
          text = amountStr,
          fontFamily = HisabMonoFontFamily,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = if (amount < 0.0) SemanticDangerLight else MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
fun TransferRowCard(
  transfer: AccountTransfer,
  currencySymbol: String,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  val fromLbl = methodLabel(transfer.fromMethod, strings)
  val toLbl = methodLabel(transfer.toMethod, strings)

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("transfer_item_${transfer.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.SwapHoriz,
          contentDescription = strings.entryTypeTransfer,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp),
        )
      }
      Spacer(Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "$fromLbl → $toLbl",
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = buildString {
            append(strings.entryTypeTransfer)
            if (transfer.note.isNotBlank()) {
              append(" • ")
              append(transfer.note)
            }
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(Modifier.width(8.dp))
      Text(
        text = CurrencyFormat.format(transfer.amount, currencySymbol),
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
      )
      IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_transfer_${transfer.id}")) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = strings.delete,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun PayableRowCard(
  payable: Payable,
  paidAmount: Double,
  currencySymbol: String,
  onClick: () -> Unit,
  onRepay: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  val method = methodLabel(payable.paymentMethod, strings)
  val remaining = (payable.amount - paidAmount).coerceAtLeast(0.0)
  val isSettled = remaining <= 0.001

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("payable_item_${payable.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(SemanticWarningLight.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Handshake,
          contentDescription = strings.entryTypeBorrow,
          tint = SemanticWarningLight,
          modifier = Modifier.size(22.dp),
        )
      }
      Spacer(Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = payable.personName,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        val statusText = if (isSettled) {
          strings.settledBadge
        } else {
          "${strings.dueAmountLabel}: ${CurrencyFormat.format(remaining, currencySymbol)}"
        }
        Text(
          text = "$method • $statusText",
          style = MaterialTheme.typography.bodyMedium,
          color = if (isSettled) SemanticSafeLight else SemanticWarningLight,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(Modifier.width(6.dp))
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "+${CurrencyFormat.format(payable.amount, currencySymbol)}",
          fontFamily = HisabMonoFontFamily,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = SemanticSafeLight,
        )
        if (!isSettled) {
          FilledTonalButton(
            onClick = onRepay,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.padding(top = 4.dp).testTag("quick_repay_${payable.id}"),
          ) {
            Text(strings.repayButton, style = MaterialTheme.typography.labelSmall)
          }
        }
      }
      IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_payable_${payable.id}")) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = strings.delete,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun RepaymentRowCard(
  payment: PayablePayment,
  currencySymbol: String,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  val method = methodLabel(payment.paymentMethod, strings)

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("repayment_item_${payment.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.ArrowUpward,
          contentDescription = strings.entryTypeRepayment,
          tint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.size(22.dp),
        )
      }
      Spacer(Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = payment.personName,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = buildString {
            append(strings.entryTypeRepayment)
            append(" • ")
            append(method)
            if (payment.note.isNotBlank()) {
              append(" • ")
              append(payment.note)
            }
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(Modifier.width(8.dp))
      Text(
        text = "-${CurrencyFormat.format(payment.amount, currencySymbol)}",
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_repayment_${payment.id}")) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = strings.delete,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

private fun methodLabel(wireValue: String, strings: AppStrings): String =
  when (PaymentMethod.fromWire(wireValue)) {
    PaymentMethod.CASH -> strings.cash
    PaymentMethod.CARD -> strings.card
    PaymentMethod.MOBILE -> strings.mobileBanking
    PaymentMethod.BANK -> strings.bank
  }
