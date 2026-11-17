package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryCatalog
import com.example.data.model.CurrencyFormat
import com.example.data.model.CurrencyOption
import com.example.data.model.DateFormatUtils
import com.example.data.model.UiState
import com.example.data.prefs.UserSettings
import com.example.i18n.LocalStrings
import com.example.notifications.ReminderScheduler
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
import com.example.ui.theme.SemanticWarningLight
import com.example.viewmodel.DashboardUiData
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
  uiState: UiState<DashboardUiData>,
  settings: UserSettings,
  selectedMonthKey: String,
  onSaveBudget: (monthKey: String, totalLimit: Double, categoryLimits: Map<String, Double>) -> Unit,
  onToggleDailyReminder: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
  onToggleBudgetAlerts: (enabled: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val currencySymbol = CurrencyOption.fromCode(settings.currencyCode).symbol

  var hasNotifPermission by remember {
    mutableStateOf(ReminderScheduler.hasNotificationPermission(context))
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    hasNotifPermission = granted
    if (granted) {
      onToggleDailyReminder(true, settings.reminderHour, settings.reminderMinute)
    }
  }

  var showTimePicker by remember { mutableStateOf(false) }

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
      val categories = remember(strings, data.customCategories) {
        CategoryCatalog.mergeCategories(strings, data.customCategories)
      }
      val existingBudget = remember(data.budgets, selectedMonthKey) {
        data.budgets.firstOrNull { it.monthKey == selectedMonthKey }
          ?: data.budgets.firstOrNull { it.monthKey == "default" }
      }
      val monthExpenses = remember(data.expenses, selectedMonthKey) {
        data.expenses.filter { it.monthKey == selectedMonthKey }
      }

      var totalLimitText by remember(selectedMonthKey, existingBudget) {
        val v = existingBudget?.totalLimit ?: 0.0
        mutableStateOf(if (v > 0.0) v.toLong().toString() else "")
      }
      val catLimitInputs = remember(selectedMonthKey, existingBudget) {
        mutableStateMapOf<String, String>().apply {
          existingBudget?.categoryLimits?.forEach { (k, v) ->
            if (v > 0.0) put(k, v.toLong().toString())
          }
        }
      }

      val totalSpent = remember(monthExpenses) { monthExpenses.sumOf { it.amount } }
      val spentByCat = remember(monthExpenses) {
        monthExpenses.groupBy { it.categoryId }.mapValues { (_, list) -> list.sumOf { it.amount } }
      }

      LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Column {
            Text(
              text = strings.budgetSettingsTitle,
              style = MaterialTheme.typography.headlineMedium,
            )
            Text(
              text = DateFormatUtils.formatMonthLabel(selectedMonthKey, settings.language.code),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Notification Reminders Card
        item {
          Card(
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Rounded.NotificationsActive,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                  text = strings.reminderSettingsTitle,
                  style = MaterialTheme.typography.titleMedium,
                )
              }

              if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                  colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                  ),
                  shape = RoundedCornerShape(14.dp),
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Text(
                      text = strings.notificationPermNeeded,
                      style = MaterialTheme.typography.bodyMedium,
                      modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                      onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                      modifier = Modifier.testTag("grant_notif_permission_btn"),
                    ) {
                      Text(strings.grantPermission)
                    }
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(strings.dailyReminderLabel, style = MaterialTheme.typography.titleMedium)
                  Text(
                    strings.dailyReminderDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Switch(
                  checked = settings.dailyReminderEnabled,
                  onCheckedChange = { checked ->
                    if (checked && !hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                      permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                      onToggleDailyReminder(checked, settings.reminderHour, settings.reminderMinute)
                    }
                  },
                  modifier = Modifier.testTag("daily_reminder_switch"),
                )
              }

              if (settings.dailyReminderEnabled) {
                val formattedTime = String.format(
                  Locale.US,
                  "%02d:%02d",
                  settings.reminderHour,
                  settings.reminderMinute,
                )
                OutlinedButton(
                  onClick = { showTimePicker = true },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Icon(Icons.Rounded.Schedule, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text("${strings.reminderTimeLabel}: $formattedTime")
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(strings.budgetAlertsLabel, style = MaterialTheme.typography.titleMedium)
                  Text(
                    strings.budgetAlertsDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Switch(
                  checked = settings.budgetAlertsEnabled,
                  onCheckedChange = { checked ->
                    if (checked && !hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                      permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    onToggleBudgetAlerts(checked)
                  },
                  modifier = Modifier.testTag("budget_alerts_switch"),
                )
              }
            }
          }
        }

        // Overall Monthly Budget Card
        item {
          val parsedTotal = totalLimitText.toDoubleOrNull() ?: 0.0
          val ratio = if (parsedTotal > 0.0) (totalSpent / parsedTotal).toFloat().coerceIn(0f, 1f) else 0f
          val barColor = when {
            parsedTotal > 0 && totalSpent >= parsedTotal -> SemanticDangerLight
            parsedTotal > 0 && totalSpent >= parsedTotal * 0.8 -> SemanticWarningLight
            else -> SemanticSafeLight
          }

          Card(
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = strings.monthlyBudgetLimit,
                style = MaterialTheme.typography.titleMedium,
              )

              OutlinedTextField(
                value = totalLimitText,
                onValueChange = { input ->
                  val clean = input.filter { it.isDigit() || it == '.' }
                  if (clean.count { it == '.' } <= 1) totalLimitText = clean
                },
                label = { Text("${strings.monthlyBudgetLimit} ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("monthly_budget_input"),
              )

              if (parsedTotal > 0.0) {
                LinearProgressIndicator(
                  progress = { ratio },
                  color = barColor,
                  trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                )
                Text(
                  text = "${CurrencyFormat.format(totalSpent, currencySymbol)} ${strings.spentOf} ${CurrencyFormat.format(parsedTotal, currencySymbol)} (${(ratio * 100).toInt()}%)",
                  fontFamily = HisabMonoFontFamily,
                  style = MaterialTheme.typography.labelMedium,
                  color = barColor,
                )
              }
            }
          }
        }

        item {
          Text(
            text = strings.categoryBudgetsTitle,
            style = MaterialTheme.typography.titleLarge,
          )
        }

        items(categories, key = { it.id }) { cat ->
          val spent = spentByCat[cat.id] ?: 0.0
          val limitText = catLimitInputs[cat.id] ?: ""
          val limitVal = limitText.toDoubleOrNull() ?: 0.0
          val ratio = if (limitVal > 0.0) (spent / limitVal).toFloat().coerceIn(0f, 1f) else 0f
          val barColor = when {
            limitVal > 0 && spent >= limitVal -> SemanticDangerLight
            limitVal > 0 && spent >= limitVal * 0.8 -> SemanticWarningLight
            else -> Color(cat.colorHex)
          }

          Card(
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(cat.colorHex).copy(alpha = 0.14f)),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = CategoryCatalog.iconFor(cat.iconKey),
                    contentDescription = cat.name,
                    tint = Color(cat.colorHex),
                    modifier = Modifier.size(18.dp),
                  )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(cat.name, style = MaterialTheme.typography.titleMedium)
                  Text(
                    text = "${strings.totalSpent}: ${CurrencyFormat.format(spent, currencySymbol)}",
                    fontFamily = HisabMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                OutlinedTextField(
                  value = limitText,
                  onValueChange = { input ->
                    val clean = input.filter { it.isDigit() || it == '.' }
                    if (clean.count { it == '.' } <= 1) catLimitInputs[cat.id] = clean
                  },
                  placeholder = { Text("0") },
                  prefix = { Text(currencySymbol) },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.width(130.dp),
                )
              }

              if (limitVal > 0.0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                  progress = { ratio },
                  color = barColor,
                  trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                )
              }
            }
          }
        }

        item {
          Button(
            onClick = {
              val totalVal = totalLimitText.toDoubleOrNull() ?: 0.0
              val catMap = catLimitInputs.entries
                .mapNotNull { (k, v) ->
                  val d = v.toDoubleOrNull()
                  if (d != null && d > 0.0) k to d else null
                }.toMap()
              onSaveBudget(selectedMonthKey, totalVal, catMap)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("save_budget_button"),
            shape = RoundedCornerShape(16.dp),
          ) {
            Text(strings.saveBudget, style = MaterialTheme.typography.titleMedium)
          }
        }
      }
    }
  }

  if (showTimePicker) {
    val timeState = rememberTimePickerState(
      initialHour = settings.reminderHour,
      initialMinute = settings.reminderMinute,
      is24Hour = false,
    )
    AlertDialog(
      onDismissRequest = { showTimePicker = false },
      title = { Text(strings.reminderTimeLabel) },
      text = { TimePicker(state = timeState) },
      confirmButton = {
        TextButton(
          onClick = {
            onToggleDailyReminder(true, timeState.hour, timeState.minute)
            showTimePicker = false
          }
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = { showTimePicker = false }) { Text(strings.cancel) }
      },
    )
  }
}
