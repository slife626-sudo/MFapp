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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryCatalog
import com.example.data.model.CategoryFlowType
import com.example.data.model.CategoryItem
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.DateFormatUtils
import com.example.data.model.PeriodFilter
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.LocalStrings
import com.example.ui.components.AddCustomCategoryDialog
import com.example.ui.components.PeriodFilterStrip
import com.example.ui.components.SensitiveAmountText
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.viewmodel.DashboardUiData

private enum class CategoryTabFilter {
  ALL,
  EXPENSE,
  INCOME,
  CUSTOM,
}

@Composable
fun CategoriesScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  selectedPeriod: PeriodFilter,
  selectedMonthKey: String,
  onSelectPeriod: (PeriodFilter) -> Unit,
  onShiftMonth: (Int) -> Unit,
  onAddCustomCategory: (name: String, iconKey: String, colorHex: Long, flowType: CategoryFlowType) -> Unit,
  onUpdateCustomCategory: (id: String, name: String, iconKey: String, colorHex: Long, flowType: CategoryFlowType) -> Unit,
  onDeleteCustomCategory: (String) -> Unit,
  onBulkDeleteCustomCategories: (List<String>) -> Unit,
) {
  val strings = LocalStrings.current
  val symbol = CurrencyOption.fromCode(settings.currencyCode).symbol
  val langCode = settings.language.code

  var tabFilter by remember { mutableStateOf(CategoryTabFilter.ALL) }
  var showAddDialog by remember { mutableStateOf(false) }
  var editingCategory by remember { mutableStateOf<CategoryItem?>(null) }
  var selectionMode by remember { mutableStateOf(false) }
  var selectedIds by remember { mutableStateOf(emptySet<String>()) }
  var confirmBulkDelete by remember { mutableStateOf(false) }
  var pendingSingleDelete by remember { mutableStateOf<CategoryItem?>(null) }

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
      }
      val filteredCategories = remember(allCategories, tabFilter) {
        when (tabFilter) {
          CategoryTabFilter.ALL -> allCategories
          CategoryTabFilter.EXPENSE -> allCategories.filter { it.flowType == CategoryFlowType.EXPENSE }
          CategoryTabFilter.INCOME -> allCategories.filter { it.flowType == CategoryFlowType.INCOME }
          CategoryTabFilter.CUSTOM -> allCategories.filter { it.isCustom }
        }
      }

      val periodExpenses = remember(data.expenses, startMillis, endMillis) {
        data.expenses.filter { it.dateMillis in startMillis..endMillis }
      }
      val periodIncomes = remember(data.incomes, startMillis, endMillis) {
        data.incomes.filter { it.dateMillis in startMillis..endMillis }
      }
      val activeBudget = remember(data.budgets, selectedMonthKey) {
        data.budgets.firstOrNull { it.monthKey == selectedMonthKey }
          ?: data.budgets.firstOrNull { it.monthKey == "default" }
      }

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
                IconButton(onClick = { selectionMode = false; selectedIds = emptySet() }) {
                  Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                }
                Text(
                  text = "${selectedIds.size} ${strings.selectedCountLabel}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              Button(
                onClick = { confirmBulkDelete = true },
                enabled = selectedIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("bulk_delete_categories_btn"),
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
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                modifier = Modifier
                  .weight(1f)
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                FilterChip(
                  selected = tabFilter == CategoryTabFilter.ALL,
                  onClick = { tabFilter = CategoryTabFilter.ALL },
                  label = { Text("${strings.allFilterTab} (${allCategories.size})") },
                  modifier = Modifier.testTag("cat_tab_all"),
                )
                FilterChip(
                  selected = tabFilter == CategoryTabFilter.EXPENSE,
                  onClick = { tabFilter = CategoryTabFilter.EXPENSE },
                  label = { Text(strings.expenseCategoryType) },
                  modifier = Modifier.testTag("cat_tab_expense"),
                )
                FilterChip(
                  selected = tabFilter == CategoryTabFilter.INCOME,
                  onClick = { tabFilter = CategoryTabFilter.INCOME },
                  label = { Text(strings.incomeCategoryType) },
                  modifier = Modifier.testTag("cat_tab_income"),
                )
                FilterChip(
                  selected = tabFilter == CategoryTabFilter.CUSTOM,
                  onClick = { tabFilter = CategoryTabFilter.CUSTOM },
                  label = { Text(strings.customBadge) },
                  modifier = Modifier.testTag("cat_tab_custom"),
                )
              }
              Spacer(Modifier.width(8.dp))
              IconButton(
                onClick = {
                  selectionMode = !selectionMode
                  if (!selectionMode) selectedIds = emptySet()
                },
                modifier = Modifier.testTag("categories_multiselect_toggle"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.CheckBox,
                  contentDescription = strings.selectedCountLabel,
                  tint = if (selectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              FilledTonalButton(
                onClick = { showAddDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("categories_add_button"),
              ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.addCustomCategory, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(strings.addCustomCategory, maxLines = 1)
              }
            }
          }

          items(filteredCategories, key = { it.id }) { cat ->
            val isIncome = cat.flowType == CategoryFlowType.INCOME
            val matchingTotal = if (isIncome) {
              periodIncomes.filter { (it.categoryId ?: "inc_other") == cat.id }.sumOf { it.amount }
            } else {
              periodExpenses.filter { it.categoryId == cat.id }.sumOf { it.amount }
            }
            val matchingCount = if (isIncome) {
              periodIncomes.count { (it.categoryId ?: "inc_other") == cat.id }
            } else {
              periodExpenses.count { it.categoryId == cat.id }
            }
            val catBudgetLimit = if (!isIncome) (activeBudget?.categoryLimits?.get(cat.id) ?: 0.0) else 0.0
            val isSelected = cat.id in selectedIds

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
                  if (selectionMode && cat.isCustom) {
                    selectedIds = if (isSelected) selectedIds - cat.id else selectedIds + cat.id
                  } else if (cat.isCustom) {
                    editingCategory = cat
                  }
                }
                .testTag("category_row_${cat.id}"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                if (selectionMode && cat.isCustom) {
                  Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                      selectedIds = if (isSelected) selectedIds - cat.id else selectedIds + cat.id
                    },
                  )
                }
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(cat.colorHex).copy(alpha = 0.16f)),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = CategoryCatalog.iconFor(cat.iconKey),
                    contentDescription = cat.name,
                    tint = Color(cat.colorHex),
                    modifier = Modifier.size(22.dp),
                  )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Text(
                      text = cat.name,
                      style = MaterialTheme.typography.titleMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f, fill = false),
                    )
                    Surface(
                      color = (if (isIncome) SemanticSafeLight else SemanticDangerLight).copy(alpha = 0.12f),
                      shape = RoundedCornerShape(50),
                    ) {
                      Text(
                        text = if (isIncome) strings.incomeCategoryType else strings.expenseCategoryType,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isIncome) SemanticSafeLight else SemanticDangerLight,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                      )
                    }
                    if (cat.isCustom) {
                      Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50),
                      ) {
                        Text(
                          text = strings.customBadge,
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSecondaryContainer,
                          modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                      }
                    }
                  }
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                      text = "$matchingCount ${strings.transactionsCount}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (catBudgetLimit > 0.0) {
                      Text(
                        text = "• ${strings.navBudgets}: ${CurrencyFormat.format(catBudgetLimit, symbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                      )
                    }
                  }
                }
                Spacer(Modifier.width(8.dp))
                SensitiveAmountText(
                  text = CurrencyFormat.format(matchingTotal, symbol),
                  blurBalances = settings.blurBalances,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isIncome) SemanticSafeLight else MaterialTheme.colorScheme.onSurface,
                )
                if (cat.isCustom && !selectionMode) {
                  IconButton(
                    onClick = { editingCategory = cat },
                    modifier = Modifier.testTag("edit_category_${cat.id}"),
                  ) {
                    Icon(Icons.Rounded.Edit, contentDescription = strings.editExpense, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  IconButton(
                    onClick = { pendingSingleDelete = cat },
                    modifier = Modifier.testTag("delete_category_${cat.id}"),
                  ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AddCustomCategoryDialog(
      langCode = langCode,
      onDismiss = { showAddDialog = false },
      onConfirmWithType = { name, iconKey, colorHex, flowType ->
        onAddCustomCategory(name, iconKey, colorHex, flowType)
        showAddDialog = false
      },
    )
  }

  editingCategory?.let { cat ->
    AddCustomCategoryDialog(
      initialName = cat.name,
      initialIconKey = cat.iconKey,
      initialColorHex = cat.colorHex,
      initialFlowType = cat.flowType,
      langCode = langCode,
      isEditing = true,
      onDismiss = { editingCategory = null },
      onConfirmWithType = { name, iconKey, colorHex, flowType ->
        onUpdateCustomCategory(cat.id, name, iconKey, colorHex, flowType)
        editingCategory = null
      },
    )
  }

  pendingSingleDelete?.let { cat ->
    AlertDialog(
      onDismissRequest = { pendingSingleDelete = null },
      title = { Text(strings.confirmDeleteTitle) },
      text = { Text(cat.name) },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteCustomCategory(cat.id)
            pendingSingleDelete = null
          },
        ) {
          Text(strings.delete, color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = { TextButton(onClick = { pendingSingleDelete = null }) { Text(strings.cancel) } },
    )
  }

  if (confirmBulkDelete) {
    AlertDialog(
      onDismissRequest = { confirmBulkDelete = false },
      title = { Text(strings.confirmDeleteTitle) },
      text = { Text("${selectedIds.size} ${strings.selectedCountLabel}") },
      confirmButton = {
        TextButton(
          onClick = {
            onBulkDeleteCustomCategories(selectedIds.toList())
            selectedIds = emptySet()
            selectionMode = false
            confirmBulkDelete = false
          },
        ) {
          Text(strings.delete, color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = { TextButton(onClick = { confirmBulkDelete = false }) { Text(strings.cancel) } },
    )
  }
}
