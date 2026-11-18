package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AccountTransfer
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.DateFormatUtils
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.PaymentMethod
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.DashboardUiData

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
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  var transferPendingDelete by remember { mutableStateOf<AccountTransfer?>(null) }
  var payablePendingDelete by remember { mutableStateOf<Payable?>(null) }
  var repaymentPendingDelete by remember { mutableStateOf<PayablePayment?>(null) }

  when (uiState) {
    UiState.Loading -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }

    is UiState.Error -> {
      Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
      }
    }

    is UiState.Success -> {
      val data = uiState.data
      val balances = remember(data) { data.accountBalances() }
      val totalUnpaidPayables = remember(data) { data.totalUnpaidPayables() }
      val unpaidPayables = remember(data.payables, data.payablePayments) {
        data.payables.filter { p -> (p.amount - data.paidAmountForPayable(p)) > 0.001 }
      }
      val settledPayables = remember(data.payables, data.payablePayments) {
        data.payables.filter { p -> (p.amount - data.paidAmountForPayable(p)) <= 0.001 }
      }

      LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item {
          Text(
            text = strings.walletsScreenTitle,
            style = MaterialTheme.typography.headlineMedium,
          )
        }

        // Total Summary Card
        item {
          val primary = MaterialTheme.colorScheme.primary
          val tertiary = MaterialTheme.colorScheme.tertiary
          Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.linearGradient(
                    colors = listOf(primary, primary.copy(alpha = 0.85f), tertiary.copy(alpha = 0.8f))
                  )
                )
                .padding(20.dp),
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = strings.overallWalletBalance,
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White.copy(alpha = 0.85f),
                )
                val totalStr = if (balances.total < 0.0) {
                  "-${CurrencyFormat.format(-balances.total, currencySymbol)}"
                } else {
                  CurrencyFormat.format(balances.total, currencySymbol)
                }
                Text(
                  text = totalStr,
                  fontFamily = HisabMonoFontFamily,
                  style = MaterialTheme.typography.displayLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                )

                if (totalUnpaidPayables > 0.0) {
                  Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp),
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = strings.totalPayablesLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                      )
                      Text(
                        text = CurrencyFormat.format(totalUnpaidPayables, currencySymbol),
                        fontFamily = HisabMonoFontFamily,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE0B2),
                      )
                    }
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  QuickWhiteActionChip(
                    icon = Icons.Rounded.Add,
                    label = strings.balanceTypeTab,
                    onClick = onAddBalance,
                    modifier = Modifier.weight(1f).testTag("wallet_add_balance_btn"),
                  )
                  QuickWhiteActionChip(
                    icon = Icons.Rounded.SwapHoriz,
                    label = strings.quickTransferAction,
                    onClick = onOpenTransfer,
                    modifier = Modifier.weight(1f).testTag("wallet_transfer_btn"),
                  )
                  QuickWhiteActionChip(
                    icon = Icons.Rounded.Add,
                    label = strings.quickBorrowAction,
                    onClick = onOpenBorrow,
                    modifier = Modifier.weight(1f).testTag("wallet_borrow_btn"),
                  )
                }
              }
            }
          }
        }

        // Per-Account Balances
        item {
          Text(
            text = strings.accountBalancesTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp),
          )
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              AccountBalanceTile(
                title = strings.walletCashLabel,
                amount = balances.cash,
                currencySymbol = currencySymbol,
                icon = Icons.Rounded.Payments,
                tint = SemanticSafeLight,
                modifier = Modifier.weight(1f),
              )
              AccountBalanceTile(
                title = strings.bank,
                amount = balances.bank,
                currencySymbol = currencySymbol,
                icon = Icons.Rounded.AccountBalance,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              AccountBalanceTile(
                title = strings.mobileBanking,
                amount = balances.mobile,
                currencySymbol = currencySymbol,
                icon = Icons.Rounded.PhoneAndroid,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
              )
              AccountBalanceTile(
                title = strings.card,
                amount = balances.card,
                currencySymbol = currencySymbol,
                icon = Icons.Rounded.CreditCard,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // Unpaid Payables Section
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = strings.unpaidPayablesTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            TextButton(onClick = onOpenBorrow) {
              Text(strings.quickBorrowAction)
            }
          }
        }

        if (unpaidPayables.isEmpty()) {
          item {
            Card(
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              ),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.CheckCircle,
                  contentDescription = null,
                  tint = SemanticSafeLight,
                )
                Text(
                  text = strings.noPayablesEmpty,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        } else {
          items(unpaidPayables, key = { "unpaid_${it.id}" }) { payable ->
            val paidSum = data.paidAmountForPayable(payable)
            val payments = data.payablePayments.filter { it.payableId == payable.id }
            PayableCard(
              payable = payable,
              paidAmount = paidSum,
              payments = payments,
              currencySymbol = currencySymbol,
              langCode = settings.language.code,
              strings = strings,
              onRepay = { onRepayPayable(payable) },
              onEdit = { onEditPayable(payable) },
              onDelete = { payablePendingDelete = payable },
              onDeletePayment = { repaymentPendingDelete = it },
            )
          }
        }

        // Settled Payables
        if (settledPayables.isNotEmpty()) {
          item {
            Text(
              text = strings.settledPayablesTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(top = 6.dp),
            )
          }
          items(settledPayables, key = { "settled_${it.id}" }) { payable ->
            val paidSum = data.paidAmountForPayable(payable)
            val payments = data.payablePayments.filter { it.payableId == payable.id }
            PayableCard(
              payable = payable,
              paidAmount = paidSum,
              payments = payments,
              currencySymbol = currencySymbol,
              langCode = settings.language.code,
              strings = strings,
              onRepay = null,
              onEdit = { onEditPayable(payable) },
              onDelete = { payablePendingDelete = payable },
              onDeletePayment = { repaymentPendingDelete = it },
            )
          }
        }

        // Transfers History
        if (data.transfers.isNotEmpty()) {
          item {
            Text(
              text = strings.transferHistoryTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(top = 6.dp),
            )
          }
          items(data.transfers.take(20), key = { "tr_${it.id}" }) { transfer ->
            TransferHistoryCard(
              transfer = transfer,
              currencySymbol = currencySymbol,
              langCode = settings.language.code,
              strings = strings,
              onClick = { onEditTransfer(transfer) },
              onDelete = { transferPendingDelete = transfer },
            )
          }
        }
      }
    }
  }

  transferPendingDelete?.let { tr ->
    AlertDialog(
      onDismissRequest = { transferPendingDelete = null },
      title = { Text(strings.confirmDeleteTransferTitle) },
      text = { Text(strings.confirmDeleteTransferMessage) },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteTransfer(tr)
            transferPendingDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) { Text(strings.delete) }
      },
      dismissButton = {
        TextButton(onClick = { transferPendingDelete = null }) { Text(strings.cancel) }
      },
    )
  }

  payablePendingDelete?.let { pay ->
    AlertDialog(
      onDismissRequest = { payablePendingDelete = null },
      title = { Text(strings.confirmDeletePayableTitle) },
      text = { Text("${pay.personName} — ${strings.confirmDeletePayableMessage}") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeletePayable(pay)
            payablePendingDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) { Text(strings.delete) }
      },
      dismissButton = {
        TextButton(onClick = { payablePendingDelete = null }) { Text(strings.cancel) }
      },
    )
  }

  repaymentPendingDelete?.let { pmt ->
    AlertDialog(
      onDismissRequest = { repaymentPendingDelete = null },
      title = { Text(strings.confirmDeleteRepaymentTitle) },
      text = { Text("${pmt.personName} — ${strings.confirmDeleteRepaymentMessage}") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteRepayment(pmt)
            repaymentPendingDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) { Text(strings.delete) }
      },
      dismissButton = {
        TextButton(onClick = { repaymentPendingDelete = null }) { Text(strings.cancel) }
      },
    )
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
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
      Spacer(Modifier.width(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun AccountBalanceTile(
  title: String,
  amount: Double,
  currencySymbol: String,
  icon: ImageVector,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(18.dp),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(18.dp))
        }
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      val amountStr = if (amount < 0.0) {
        "-${CurrencyFormat.format(-amount, currencySymbol)}"
      } else {
        CurrencyFormat.format(amount, currencySymbol)
      }
      Text(
        text = amountStr,
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = if (amount < 0.0) SemanticDangerLight else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun PayableCard(
  payable: Payable,
  paidAmount: Double,
  payments: List<PayablePayment>,
  currencySymbol: String,
  langCode: String,
  strings: AppStrings,
  onRepay: (() -> Unit)?,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onDeletePayment: (PayablePayment) -> Unit,
) {
  val remaining = (payable.amount - paidAmount).coerceAtLeast(0.0)
  val isSettled = remaining <= 0.001
  val progress = if (payable.amount > 0.0) (paidAmount / payable.amount).toFloat().coerceIn(0f, 1f) else 1f
  val methodLabel = paymentMethodLabel(payable.paymentMethod, strings)

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(18.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onEdit),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = payable.personName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "$methodLabel • ${DateFormatUtils.formatDateCompact(payable.dateMillis, langCode)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (isSettled) {
          Surface(
            color = SemanticSafeLight.copy(alpha = 0.16f),
            shape = RoundedCornerShape(50),
          ) {
            Text(
              text = strings.settledBadge,
              style = MaterialTheme.typography.labelMedium,
              color = SemanticSafeLight,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        } else if (onRepay != null) {
          FilledTonalButton(
            onClick = onRepay,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("repay_btn_${payable.id}"),
          ) {
            Text(strings.repayButton)
          }
        }
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Rounded.DeleteOutline,
            contentDescription = strings.delete,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      LinearProgressIndicator(
        progress = { progress },
        color = if (isSettled) SemanticSafeLight else SemanticWarningLight,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "${strings.paidAmountLabel}: ${CurrencyFormat.format(paidAmount, currencySymbol)} / ${CurrencyFormat.format(payable.amount, currencySymbol)}",
          fontFamily = HisabMonoFontFamily,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!isSettled) {
          Text(
            text = "${strings.dueAmountLabel}: ${CurrencyFormat.format(remaining, currencySymbol)}",
            fontFamily = HisabMonoFontFamily,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SemanticWarningLight,
          )
        }
      }

      if (payable.note.isNotBlank()) {
        Text(
          text = payable.note,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      if (payments.isNotEmpty()) {
        Spacer(Modifier.height(2.dp))
        Text(
          text = strings.repaymentHistoryTitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
              Text(
                text = "${paymentMethodLabel(pmt.paymentMethod, strings)} • ${DateFormatUtils.formatDateCompact(pmt.dateMillis, langCode)}",
                style = MaterialTheme.typography.labelMedium,
              )
              if (pmt.note.isNotBlank()) {
                Text(
                  text = pmt.note,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
            Text(
              text = "-${CurrencyFormat.format(pmt.amount, currencySymbol)}",
              fontFamily = HisabMonoFontFamily,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
            )
            IconButton(
              onClick = { onDeletePayment(pmt) },
              modifier = Modifier.size(32.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = strings.delete,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TransferHistoryCard(
  transfer: AccountTransfer,
  currencySymbol: String,
  langCode: String,
  strings: AppStrings,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val fromLabel = paymentMethodLabel(transfer.fromMethod, strings)
  val toLabel = paymentMethodLabel(transfer.toMethod, strings)
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick),
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
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.SwapHoriz,
          contentDescription = strings.entryTypeTransfer,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(20.dp),
        )
      }
      Spacer(Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "$fromLabel → $toLabel",
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = buildString {
            append(DateFormatUtils.formatDateCompact(transfer.dateMillis, langCode))
            if (transfer.note.isNotBlank()) {
              append(" • ")
              append(transfer.note)
            }
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = CurrencyFormat.format(transfer.amount, currencySymbol),
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      IconButton(onClick = onDelete) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = strings.delete,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

private fun paymentMethodLabel(wireValue: String, strings: AppStrings): String =
  when (PaymentMethod.fromWire(wireValue)) {
    PaymentMethod.CASH -> strings.cash
    PaymentMethod.CARD -> strings.card
    PaymentMethod.MOBILE -> strings.mobileBanking
    PaymentMethod.BANK -> strings.bank
  }
