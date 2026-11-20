package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.DateFormatUtils
import com.example.data.model.TrashItem
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.components.SensitiveAmountText
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.DashboardUiData

@Composable
fun TrashScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  onRestoreItem: (TrashItem) -> Unit,
  onDeleteForever: (String) -> Unit,
  onEmptyTrash: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val symbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  val langCode = settings.language.code
  var pendingPermanentDelete by remember { mutableStateOf<TrashItem?>(null) }
  var confirmEmptyTrash by remember { mutableStateOf(false) }

  when (uiState) {
    UiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    is UiState.Error -> Box(
      modifier = modifier
        .fillMaxSize()
        .padding(24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(uiState.message, color = MaterialTheme.colorScheme.error)
    }
    is UiState.Success -> {
      val trashItems = uiState.data.trashItems
      val nowMillis = System.currentTimeMillis()

      LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item {
          Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${strings.navTrash} (${trashItems.size})",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
                Text(
                  text = strings.trashEmptyBody,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              if (trashItems.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                  onClick = { confirmEmptyTrash = true },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                  modifier = Modifier
                    .height(48.dp)
                    .testTag("empty_trash_button"),
                ) {
                  Text(strings.emptyTrashButton)
                }
              }
            }
          }
        }

        if (trashItems.isEmpty()) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
              shape = RoundedCornerShape(22.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = strings.trashEmptyTitle,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp),
                  )
                }
                Text(
                  text = strings.trashEmptyTitle,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  textAlign = TextAlign.Center,
                )
                Text(
                  text = strings.trashEmptyBody,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                )
              }
            }
          }
        } else {
          items(trashItems, key = { it.id }) { item ->
            val daysElapsed = ((nowMillis - item.deletedAtMillis) / 86_400_000L).toInt().coerceAtLeast(0)
            val daysRemaining = (30 - daysElapsed).coerceIn(0, 30)
            val (typeLabel, icon, tint) = trashVisuals(item.entityType, strings)

            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("trash_item_${item.id}"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .clip(CircleShape)
                      .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = icon,
                      contentDescription = typeLabel,
                      tint = tint,
                      modifier = Modifier.size(20.dp),
                    )
                  }
                  Spacer(Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                      text = item.title.ifBlank { typeLabel },
                      style = MaterialTheme.typography.titleMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = "$typeLabel • ${DateFormatUtils.formatDateCompact(item.deletedAtMillis, langCode)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  Spacer(Modifier.width(8.dp))
                  SensitiveAmountText(
                    text = CurrencyFormat.format(item.amount, symbol),
                    blurBalances = settings.blurBalances,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tint,
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(50),
                  ) {
                    Text(
                      text = "$daysRemaining ${strings.daysRemainingSuffix}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                  }

                  Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    FilledTonalButton(
                      onClick = { onRestoreItem(item) },
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                      modifier = Modifier
                        .height(48.dp)
                        .testTag("restore_trash_${item.id}"),
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.RestoreFromTrash,
                        contentDescription = strings.restoreButton,
                        modifier = Modifier.size(18.dp),
                      )
                      Spacer(Modifier.width(4.dp))
                      Text(strings.restoreButton)
                    }
                    IconButton(
                      onClick = { pendingPermanentDelete = item },
                      modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_forever_trash_${item.id}"),
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.DeleteForever,
                        contentDescription = strings.deleteForeverButton,
                        tint = MaterialTheme.colorScheme.error,
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
  }

  pendingPermanentDelete?.let { item ->
    AlertDialog(
      onDismissRequest = { pendingPermanentDelete = null },
      title = { Text(strings.deleteForeverButton) },
      text = { Text("${item.title} — ${strings.confirmDeleteMessage}") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteForever(item.id)
            pendingPermanentDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_forever_btn"),
        ) {
          Text(strings.deleteForeverButton)
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingPermanentDelete = null }) {
          Text(strings.cancel)
        }
      },
    )
  }

  if (confirmEmptyTrash) {
    AlertDialog(
      onDismissRequest = { confirmEmptyTrash = false },
      title = { Text(strings.emptyTrashButton) },
      text = { Text(strings.confirmDeleteMessage) },
      confirmButton = {
        TextButton(
          onClick = {
            onEmptyTrash()
            confirmEmptyTrash = false
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_empty_trash_btn"),
        ) {
          Text(strings.emptyTrashButton)
        }
      },
      dismissButton = {
        TextButton(onClick = { confirmEmptyTrash = false }) {
          Text(strings.cancel)
        }
      },
    )
  }
}

@Composable
private fun trashVisuals(
  entityType: String,
  strings: AppStrings,
): Triple<String, androidx.compose.ui.graphics.vector.ImageVector, androidx.compose.ui.graphics.Color> =
  when (entityType.uppercase()) {
    "INCOME" -> Triple(strings.entryTypeIncome, Icons.Rounded.AccountBalanceWallet, SemanticSafeLight)
    "TRANSFER" -> Triple(strings.entryTypeTransfer, Icons.Rounded.SwapHoriz, MaterialTheme.colorScheme.primary)
    "PAYABLE" -> Triple(strings.entryTypeBorrow, Icons.Rounded.Handshake, SemanticWarningLight)
    "RECEIVABLE" -> Triple(strings.entryTypeLend, Icons.Rounded.NorthEast, MaterialTheme.colorScheme.primary)
    else -> Triple(strings.entryTypeExpense, Icons.Rounded.ReceiptLong, SemanticDangerLight)
  }
