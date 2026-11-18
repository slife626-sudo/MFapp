package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.MarketList
import com.example.data.model.MarketListItem
import com.example.data.model.PaymentMethod
import com.example.data.prefs.UserSettings
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticSafeLight
import com.example.viewmodel.AccountBalances

@Composable
fun MarketScreen(
  marketLists: List<MarketList>,
  selectedListId: String?,
  selectedItems: List<MarketListItem>,
  currentUserId: String,
  accountBalances: AccountBalances,
  settings: UserSettings,
  onBackToDashboard: () -> Unit,
  onSelectList: (String?) -> Unit,
  onCreateList: (title: String, sharedWithEmail: String, note: String) -> Unit,
  onUpdateListAndShare: (list: MarketList, title: String, sharedWithEmail: String, note: String) -> Unit,
  onToggleListCompleted: (listId: String, completed: Boolean) -> Unit,
  onDeleteList: (MarketList) -> Unit,
  onAddItem: (list: MarketList, name: String, quantity: String, estimatedPrice: Double) -> Unit,
  onUpdateItem: (listId: String, item: MarketListItem, name: String, quantity: String, estimatedPrice: Double, bought: Boolean) -> Unit,
  onToggleItemBought: (listId: String, item: MarketListItem) -> Unit,
  onDeleteItem: (listId: String, itemId: String) -> Unit,
  onLogAsExpense: (list: MarketList, totalAmount: Double, paymentMethod: PaymentMethod) -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  val selectedList = remember(marketLists, selectedListId) {
    marketLists.firstOrNull { it.id == selectedListId }
  }

  var showCreateDialog by remember { mutableStateOf(false) }
  var editingShareList by remember { mutableStateOf<MarketList?>(null) }
  var deletingList by remember { mutableStateOf<MarketList?>(null) }

  if (selectedList != null) {
    BackHandler { onSelectList(null) }
    MarketListDetailContent(
      list = selectedList,
      items = selectedItems,
      currentUserId = currentUserId,
      accountBalances = accountBalances,
      currencySymbol = currencySymbol,
      strings = strings,
      onBack = { onSelectList(null) },
      onOpenShareEdit = { editingShareList = selectedList },
      onToggleListCompleted = { onToggleListCompleted(selectedList.id, !selectedList.completed) },
      onAddItem = { name, qty, price -> onAddItem(selectedList, name, qty, price) },
      onUpdateItem = { item, name, qty, price ->
        onUpdateItem(selectedList.id, item, name, qty, price, item.bought)
      },
      onToggleItemBought = { item -> onToggleItemBought(selectedList.id, item) },
      onDeleteItem = { item -> onDeleteItem(selectedList.id, item.id) },
      onLogAsExpense = { amount, method -> onLogAsExpense(selectedList, amount, method) },
      modifier = modifier,
    )
  } else {
    BackHandler { onBackToDashboard() }
    MarketListsOverviewContent(
      marketLists = marketLists,
      currentUserId = currentUserId,
      strings = strings,
      onBack = onBackToDashboard,
      onOpenNewDialog = { showCreateDialog = true },
      onSelectList = { onSelectList(it.id) },
      onOpenShareEdit = { editingShareList = it },
      onDeleteList = { deletingList = it },
      modifier = modifier,
    )
  }

  if (showCreateDialog) {
    MarketListEditorDialog(
      initialList = null,
      strings = strings,
      onDismiss = { showCreateDialog = false },
      onConfirm = { title, email, note ->
        onCreateList(title, email, note)
        showCreateDialog = false
      },
    )
  }

  editingShareList?.let { listToEdit ->
    MarketListEditorDialog(
      initialList = listToEdit,
      strings = strings,
      onDismiss = { editingShareList = null },
      onConfirm = { title, email, note ->
        onUpdateListAndShare(listToEdit, title, email, note)
        editingShareList = null
      },
    )
  }

  deletingList?.let { target ->
    AlertDialog(
      onDismissRequest = { deletingList = null },
      title = { Text(strings.confirmDeleteMarketListTitle) },
      text = { Text("${target.title} — ${strings.confirmDeleteMarketListMessage}") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteList(target)
            deletingList = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
          Text(strings.delete)
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingList = null }) { Text(strings.cancel) }
      },
    )
  }
}

@Composable
private fun MarketListsOverviewContent(
  marketLists: List<MarketList>,
  currentUserId: String,
  strings: AppStrings,
  onBack: () -> Unit,
  onOpenNewDialog: () -> Unit,
  onSelectList: (MarketList) -> Unit,
  onOpenShareEdit: (MarketList) -> Unit,
  onDeleteList: (MarketList) -> Unit,
  modifier: Modifier = Modifier,
) {
  val activeLists = remember(marketLists) { marketLists.filter { !it.completed } }
  val completedLists = remember(marketLists) { marketLists.filter { it.completed } }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack, modifier = Modifier.testTag("market_back_btn")) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
          }
          Spacer(Modifier.width(4.dp))
          Text(
            text = strings.marketListTitle,
            style = MaterialTheme.typography.headlineMedium,
          )
        }
        Button(
          onClick = onOpenNewDialog,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.testTag("create_market_list_btn"),
        ) {
          Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text(strings.marketNewListButton)
        }
      }
    }

    if (marketLists.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          ),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.ShoppingBag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp),
              )
            }
            Spacer(Modifier.height(12.dp))
            Text(
              text = strings.noMarketListsTitle,
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = strings.noMarketListsBody,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onOpenNewDialog) {
              Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text(strings.marketNewListButton)
            }
          }
        }
      }
    } else {
      if (activeLists.isNotEmpty()) {
        items(activeLists, key = { "active_${it.id}" }) { list ->
          MarketListSummaryCard(
            list = list,
            isOwner = list.ownerUid == currentUserId,
            strings = strings,
            onClick = { onSelectList(list) },
            onShareEdit = { onOpenShareEdit(list) },
            onDelete = { onDeleteList(list) },
          )
        }
      }

      if (completedLists.isNotEmpty()) {
        item {
          Text(
            text = strings.completedBadge,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
          )
        }
        items(completedLists, key = { "done_${it.id}" }) { list ->
          MarketListSummaryCard(
            list = list,
            isOwner = list.ownerUid == currentUserId,
            strings = strings,
            onClick = { onSelectList(list) },
            onShareEdit = { onOpenShareEdit(list) },
            onDelete = { onDeleteList(list) },
          )
        }
      }
    }
  }
}

@Composable
private fun MarketListSummaryCard(
  list: MarketList,
  isOwner: Boolean,
  strings: AppStrings,
  onClick: () -> Unit,
  onShareEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(18.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("market_list_card_${list.id}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
              if (list.completed) SemanticSafeLight.copy(alpha = 0.14f)
              else MaterialTheme.colorScheme.primaryContainer
            ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (list.completed) Icons.Rounded.CheckCircle else Icons.Rounded.ShoppingBag,
            contentDescription = list.title,
            tint = if (list.completed) SemanticSafeLight else MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = list.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          val shareLine = when {
            !isOwner -> "${strings.sharedByLabel}: ${list.ownerEmail.ifBlank { list.ownerName }}"
            list.sharedWithEmail.isNotBlank() -> "${strings.sharedWithLabel}: ${list.sharedWithEmail}"
            else -> list.note
          }
          if (shareLine.isNotBlank()) {
            Text(
              text = shareLine,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
        Surface(
          color = if (list.completed) SemanticSafeLight.copy(alpha = 0.14f)
          else MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = if (list.completed) strings.completedBadge else strings.activeBadge,
            style = MaterialTheme.typography.labelSmall,
            color = if (list.completed) SemanticSafeLight else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
        if (isOwner) {
          IconButton(onClick = onShareEdit, modifier = Modifier.testTag("share_market_list_${list.id}")) {
            Icon(
              imageVector = Icons.Rounded.Email,
              contentDescription = strings.shareListButton,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
          IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_market_list_${list.id}")) {
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
}

@Composable
private fun MarketListDetailContent(
  list: MarketList,
  items: List<MarketListItem>,
  currentUserId: String,
  accountBalances: AccountBalances,
  currencySymbol: String,
  strings: AppStrings,
  onBack: () -> Unit,
  onOpenShareEdit: () -> Unit,
  onToggleListCompleted: () -> Unit,
  onAddItem: (name: String, quantity: String, estimatedPrice: Double) -> Unit,
  onUpdateItem: (item: MarketListItem, name: String, quantity: String, estimatedPrice: Double) -> Unit,
  onToggleItemBought: (MarketListItem) -> Unit,
  onDeleteItem: (MarketListItem) -> Unit,
  onLogAsExpense: (totalAmount: Double, paymentMethod: PaymentMethod) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val isOwner = list.ownerUid == currentUserId

  var itemName by remember { mutableStateOf("") }
  var itemQty by remember { mutableStateOf("") }
  var itemPriceText by remember { mutableStateOf("") }

  var editingItem by remember { mutableStateOf<MarketListItem?>(null) }
  var showLogExpenseDialog by remember { mutableStateOf(false) }

  val boughtCount = remember(items) { items.count { it.bought } }
  val totalCount = items.size
  val progress = if (totalCount > 0) boughtCount.toFloat() / totalCount.toFloat() else 0f
  val estimatedTotal = remember(items) { items.sumOf { it.estimatedPrice } }
  val boughtPriceSum = remember(items) { items.filter { it.bought }.sumOf { it.estimatedPrice } }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // Top Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = list.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          val shareDesc = when {
            !isOwner -> "${strings.sharedByLabel}: ${list.ownerEmail.ifBlank { list.ownerName }}"
            list.sharedWithEmail.isNotBlank() -> "${strings.sharedWithLabel}: ${list.sharedWithEmail}"
            else -> list.note
          }
          if (shareDesc.isNotBlank()) {
            Text(
              text = shareDesc,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
        if (isOwner) {
          IconButton(onClick = onOpenShareEdit, modifier = Modifier.testTag("edit_share_email_btn")) {
            Icon(
              imageVector = Icons.Rounded.Email,
              contentDescription = strings.shareListButton,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }
        IconButton(
          onClick = {
            val formattedText = buildString {
              appendLine(list.title)
              items.forEachIndexed { idx, itm ->
                val check = if (itm.bought) "[x]" else "[ ]"
                val qtyPart = if (itm.quantity.isNotBlank()) " (${itm.quantity})" else ""
                val pricePart = if (itm.estimatedPrice > 0.0) " - ${CurrencyFormat.format(itm.estimatedPrice, currencySymbol)}" else ""
                appendLine("${idx + 1}. $check ${itm.name}$qtyPart$pricePart")
              }
            }
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_SUBJECT, list.title)
              putExtra(Intent.EXTRA_TEXT, formattedText)
            }
            context.startActivity(Intent.createChooser(sendIntent, strings.shareViaTextButton))
          },
          modifier = Modifier.testTag("share_text_intent_btn"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Share,
            contentDescription = strings.shareViaTextButton,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    // Progress & Totals Card
    item {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "${strings.boughtLabel}: $boughtCount / $totalCount",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            if (estimatedTotal > 0.0) {
              Text(
                text = "${strings.estimatedTotalLabel}: ${CurrencyFormat.format(estimatedTotal, currencySymbol)}",
                fontFamily = HisabMonoFontFamily,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
          LinearProgressIndicator(
            progress = { progress },
            color = SemanticSafeLight,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
              .fillMaxWidth()
              .height(7.dp)
              .clip(RoundedCornerShape(4.dp)),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FilledTonalButton(
              onClick = { showLogExpenseDialog = true },
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("log_market_expense_btn"),
            ) {
              Icon(Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text(
                text = strings.logAsExpenseButton,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            OutlinedButton(
              onClick = onToggleListCompleted,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              modifier = Modifier.testTag("toggle_list_completed_btn"),
            ) {
              Icon(
                imageVector = if (list.completed) Icons.Rounded.Replay else Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(if (list.completed) strings.reopenListButton else strings.markCompletedButton)
            }
          }
        }
      }
    }

    // Inline Quick Add Item Card
    item {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = itemName,
            onValueChange = { if (it.length <= 120) itemName = it },
            label = { Text(strings.itemNameLabel) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("market_item_name_input"),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = itemQty,
              onValueChange = { if (it.length <= 40) itemQty = it },
              label = { Text(strings.itemQtyLabel) },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("market_item_qty_input"),
            )
            OutlinedTextField(
              value = itemPriceText,
              onValueChange = { inp ->
                val filtered = inp.filter { it.isDigit() || it == '.' }
                if (filtered.count { it == '.' } <= 1) itemPriceText = filtered
              },
              label = { Text("${strings.itemPriceOptionalLabel} ($currencySymbol)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("market_item_price_input"),
            )
            Button(
              onClick = {
                val cleanName = itemName.trim()
                if (cleanName.isNotEmpty()) {
                  val price = itemPriceText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                  onAddItem(cleanName, itemQty.trim(), price)
                  itemName = ""
                  itemQty = ""
                  itemPriceText = ""
                }
              },
              enabled = itemName.trim().isNotEmpty(),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("add_market_item_btn"),
            ) {
              Icon(Icons.Rounded.Add, contentDescription = strings.addItemButton)
            }
          }
        }
      }
    }

    // Items List
    items(items, key = { it.id }) { item ->
      MarketItemChecklistRow(
        item = item,
        currencySymbol = currencySymbol,
        strings = strings,
        onToggleBought = { onToggleItemBought(item) },
        onEdit = { editingItem = item },
        onDelete = { onDeleteItem(item) },
      )
    }
  }

  editingItem?.let { target ->
    EditMarketItemDialog(
      item = target,
      currencySymbol = currencySymbol,
      strings = strings,
      onDismiss = { editingItem = null },
      onConfirm = { newName, newQty, newPrice ->
        onUpdateItem(target, newName, newQty, newPrice)
        editingItem = null
      },
    )
  }

  if (showLogExpenseDialog) {
    val defaultTotal = when {
      boughtPriceSum > 0.0 -> boughtPriceSum
      estimatedTotal > 0.0 -> estimatedTotal
      else -> 0.0
    }
    LogMarketExpenseDialog(
      listTitle = list.title,
      defaultAmount = defaultTotal,
      accountBalances = accountBalances,
      currencySymbol = currencySymbol,
      strings = strings,
      onDismiss = { showLogExpenseDialog = false },
      onConfirm = { amt, method ->
        onLogAsExpense(amt, method)
        showLogExpenseDialog = false
      },
    )
  }
}

@Composable
private fun MarketItemChecklistRow(
  item: MarketListItem,
  currencySymbol: String,
  strings: AppStrings,
  onToggleBought: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = if (item.bought) {
        MaterialTheme.colorScheme.surfaceContainerLow
      } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
      },
    ),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onToggleBought)
      .testTag("market_item_row_${item.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        checked = item.bought,
        onCheckedChange = { onToggleBought() },
        modifier = Modifier.testTag("market_item_check_${item.id}"),
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.titleMedium,
          textDecoration = if (item.bought) TextDecoration.LineThrough else TextDecoration.None,
          color = if (item.bought) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        val metaParts = buildList {
          if (item.quantity.isNotBlank()) add(item.quantity)
          if (item.estimatedPrice > 0.0) add(CurrencyFormat.format(item.estimatedPrice, currencySymbol))
        }
        if (metaParts.isNotEmpty()) {
          Text(
            text = metaParts.joinToString(" • "),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = HisabMonoFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_market_item_${item.id}")) {
        Icon(
          imageVector = Icons.Rounded.Edit,
          contentDescription = strings.editItemTitle,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
      }
      IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_market_item_${item.id}")) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = strings.delete,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun MarketListEditorDialog(
  initialList: MarketList?,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (title: String, sharedWithEmail: String, note: String) -> Unit,
) {
  var title by remember(initialList) { mutableStateOf(initialList?.title.orEmpty()) }
  var sharedEmail by remember(initialList) { mutableStateOf(initialList?.sharedWithEmail.orEmpty()) }
  var note by remember(initialList) { mutableStateOf(initialList?.note.orEmpty()) }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (initialList == null) strings.marketNewListButton else strings.shareListTitle)
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { if (it.length <= 120) { title = it; error = null } },
          label = { Text(strings.marketListNameLabel) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("market_list_title_input"),
        )
        OutlinedTextField(
          value = sharedEmail,
          onValueChange = { if (it.length <= 160) sharedEmail = it },
          label = { Text(strings.shareWithEmailLabel) },
          supportingText = { Text(strings.shareWithEmailHint) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("market_share_email_input"),
        )
        OutlinedTextField(
          value = note,
          onValueChange = { if (it.length <= 300) note = it },
          label = { Text(strings.noteOptionalLabel) },
          maxLines = 2,
          modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
          Text(
            text = error ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.trim().isEmpty()) {
            error = strings.invalidInputError
            return@Button
          }
          onConfirm(title.trim(), sharedEmail.trim().lowercase(), note.trim())
        },
        modifier = Modifier.testTag("save_market_list_btn"),
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
private fun EditMarketItemDialog(
  item: MarketListItem,
  currencySymbol: String,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (name: String, quantity: String, estimatedPrice: Double) -> Unit,
) {
  var name by remember(item.id) { mutableStateOf(item.name) }
  var qty by remember(item.id) { mutableStateOf(item.quantity) }
  var priceText by remember(item.id) {
    mutableStateOf(
      if (item.estimatedPrice > 0.0) {
        if (item.estimatedPrice % 1.0 == 0.0) item.estimatedPrice.toLong().toString()
        else item.estimatedPrice.toString()
      } else ""
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.editItemTitle) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { if (it.length <= 120) name = it },
          label = { Text(strings.itemNameLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = qty,
          onValueChange = { if (it.length <= 40) qty = it },
          label = { Text(strings.itemQtyLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = priceText,
          onValueChange = { inp ->
            val filtered = inp.filter { it.isDigit() || it == '.' }
            if (filtered.count { it == '.' } <= 1) priceText = filtered
          },
          label = { Text("${strings.itemPriceOptionalLabel} ($currencySymbol)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.trim().isNotEmpty()) {
            onConfirm(
              name.trim(),
              qty.trim(),
              priceText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
            )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogMarketExpenseDialog(
  listTitle: String,
  defaultAmount: Double,
  accountBalances: AccountBalances,
  currencySymbol: String,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (amount: Double, paymentMethod: PaymentMethod) -> Unit,
) {
  var amountText by remember(defaultAmount) {
    mutableStateOf(
      if (defaultAmount > 0.0) {
        if (defaultAmount % 1.0 == 0.0) defaultAmount.toLong().toString()
        else defaultAmount.toString()
      } else ""
    )
  }
  var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
  var validationError by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.logAsExpenseButton) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = listTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
          value = amountText,
          onValueChange = { inp ->
            val filtered = inp.filter { it.isDigit() || it == '.' }
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
            .testTag("log_market_amount_input"),
        )
        Text(
          text = strings.paymentMethodLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          PaymentMethod.entries.forEach { method ->
            val label = when (method) {
              PaymentMethod.CASH -> strings.cash
              PaymentMethod.CARD -> strings.card
              PaymentMethod.MOBILE -> strings.mobileBanking
              PaymentMethod.BANK -> strings.bank
            }
            val bal = accountBalances.forMethod(method)
            FilterChip(
              selected = selectedMethod == method,
              onClick = { selectedMethod = method },
              label = { Text("$label (${CurrencyFormat.format(bal, currencySymbol)})") },
            )
          }
        }
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
          val amt = amountText.toDoubleOrNull() ?: 0.0
          if (amt <= 0.0 || amt > 1_000_000_000.0) {
            validationError = strings.invalidInputError
            return@Button
          }
          onConfirm(amt, selectedMethod)
        },
        modifier = Modifier.testTag("confirm_log_market_expense_btn"),
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    },
  )
}
