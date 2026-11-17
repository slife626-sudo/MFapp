package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.backup.DriveAuthOutcome
import com.example.data.backup.DriveBackupFile
import com.example.data.backup.GoogleDriveBackupManager
import com.example.data.model.CategoryCatalog
import com.example.data.model.CurrencyOption
import com.example.data.model.CustomCategory
import com.example.data.model.DateFormatUtils
import com.example.data.prefs.UserSettings
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.ui.components.AddCustomCategoryDialog
import com.example.ui.components.SetupPinDialog
import com.example.ui.theme.AppThemePalette
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.paletteSwatchColor
import kotlinx.coroutines.launch

private enum class PendingDriveAction {
  UPLOAD,
  RESTORE_LIST,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
  userDisplayName: String,
  userEmail: String,
  settings: UserSettings,
  customCategories: List<CustomCategory>,
  isBusy: Boolean,
  driveFiles: List<DriveBackupFile>,
  onSignOut: () -> Unit,
  onToggleCloudSync: (Boolean) -> Unit,
  onBackupToDrive: (accessToken: String) -> Unit,
  onFetchDriveBackups: (accessToken: String, onReady: () -> Unit) -> Unit,
  onRestoreDriveFile: (accessToken: String, fileId: String) -> Unit,
  onBuildCsvString: () -> String,
  onBuildJsonString: () -> String,
  onImportJsonString: (String) -> Unit,
  onEnablePin: (String) -> Unit,
  onDisablePin: () -> Unit,
  onToggleLockOnBackground: (Boolean) -> Unit,
  onSelectThemeMode: (ThemeMode) -> Unit,
  onSelectThemePalette: (AppThemePalette) -> Unit,
  onSelectLanguage: (AppLanguage) -> Unit,
  onSelectCurrency: (String) -> Unit,
  onAddCustomCategory: (name: String, iconKey: String, colorHex: Long) -> Unit,
  onDeleteCustomCategory: (String) -> Unit,
  onNotifyMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val strings = LocalStrings.current

  var showPinSetupDialog by remember { mutableStateOf(false) }
  var showCustomCatDialog by remember { mutableStateOf(false) }
  var showDriveRestoreDialog by remember { mutableStateOf(false) }
  var activeDriveToken by remember { mutableStateOf<String?>(null) }
  var pendingDriveAction by remember { mutableStateOf<PendingDriveAction?>(null) }

  // Google Drive Authorization IntentSender Launcher
  val driveAuthLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartIntentSenderForResult(),
  ) { activityResult ->
    val tokenResult = GoogleDriveBackupManager.extractTokenFromIntent(context, activityResult.data)
    tokenResult.onSuccess { token ->
      activeDriveToken = token
      when (pendingDriveAction) {
        PendingDriveAction.UPLOAD -> onBackupToDrive(token)
        PendingDriveAction.RESTORE_LIST -> onFetchDriveBackups(token) {
          showDriveRestoreDialog = true
        }
        null -> {}
      }
      pendingDriveAction = null
    }.onFailure { err ->
      pendingDriveAction = null
      onNotifyMessage(err.localizedMessage ?: "Google Drive authorization cancelled")
    }
  }

  fun startDriveAction(action: PendingDriveAction) {
    val activity = context as? Activity ?: return
    pendingDriveAction = action
    scope.launch {
      val authRes = GoogleDriveBackupManager.requestDriveAuthorization(activity)
      authRes.onSuccess { outcome ->
        when (outcome) {
          is DriveAuthOutcome.Authorized -> {
            activeDriveToken = outcome.accessToken
            pendingDriveAction = null
            when (action) {
              PendingDriveAction.UPLOAD -> onBackupToDrive(outcome.accessToken)
              PendingDriveAction.RESTORE_LIST -> onFetchDriveBackups(outcome.accessToken) {
                showDriveRestoreDialog = true
              }
            }
          }
          is DriveAuthOutcome.NeedsResolution -> {
            val pi = outcome.result.pendingIntent
            if (pi != null) {
              driveAuthLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
            } else {
              pendingDriveAction = null
              onNotifyMessage("Drive consent screen unavailable")
            }
          }
        }
      }.onFailure { err ->
        pendingDriveAction = null
        onNotifyMessage(err.localizedMessage ?: "Google Drive authorization failed")
      }
    }
  }

  // Storage Access Framework (SAF) CSV Export
  val csvExportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("text/csv"),
  ) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.openOutputStream(uri)?.use { os ->
          os.write(onBuildCsvString().toByteArray(Charsets.UTF_8))
        }
        onNotifyMessage(strings.exportSuccess)
      } catch (e: Exception) {
        onNotifyMessage(e.localizedMessage ?: "Export failed")
      }
    }
  }

  // SAF JSON Export
  val jsonExportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json"),
  ) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.openOutputStream(uri)?.use { os ->
          os.write(onBuildJsonString().toByteArray(Charsets.UTF_8))
        }
        onNotifyMessage(strings.exportSuccess)
      } catch (e: Exception) {
        onNotifyMessage(e.localizedMessage ?: "Export failed")
      }
    }
  }

  // SAF JSON Import
  val jsonImportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument(),
  ) { uri ->
    if (uri != null) {
      try {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (!text.isNullOrBlank()) {
          onImportJsonString(text)
        }
      } catch (e: Exception) {
        onNotifyMessage(e.localizedMessage ?: "Import failed")
      }
    }
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Text(
        text = strings.settingsTitle,
        style = MaterialTheme.typography.headlineMedium,
      )
    }

    // Account Card
    item {
      SettingsCard(title = strings.accountSection) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = userDisplayName.ifBlank { "Google User" },
              style = MaterialTheme.typography.titleMedium,
            )
            if (userEmail.isNotBlank()) {
              Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.testTag("sign_out_button"),
          ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = strings.signOut, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.signOut)
          }
        }
      }
    }

    // Cloud Sync & Offline Mode
    item {
      SettingsCard(title = strings.syncSection, icon = Icons.Rounded.CloudSync) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(strings.cloudSyncToggle, style = MaterialTheme.typography.titleMedium)
            Text(
              strings.cloudSyncDesc,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Switch(
            checked = settings.cloudSyncEnabled,
            onCheckedChange = onToggleCloudSync,
            modifier = Modifier.testTag("cloud_sync_switch"),
          )
        }
      }
    }

    // Google Drive Backup & Restore
    item {
      SettingsCard(title = strings.driveBackupSection, icon = Icons.Rounded.Backup) {
        val lastText = if (settings.lastDriveBackupMillis > 0L) {
          DateFormatUtils.formatDateTime(settings.lastDriveBackupMillis, settings.language.code)
        } else {
          strings.neverBackedUp
        }
        Text(
          text = "${strings.lastDriveBackup}: $lastText",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = { startDriveAction(PendingDriveAction.UPLOAD) },
            enabled = !isBusy,
            modifier = Modifier
              .weight(1f)
              .testTag("drive_backup_button"),
          ) {
            if (isBusy) {
              CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
              Icon(Icons.Rounded.Backup, contentDescription = null, Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text(strings.backUpToDrive)
            }
          }
          OutlinedButton(
            onClick = { startDriveAction(PendingDriveAction.RESTORE_LIST) },
            enabled = !isBusy,
            modifier = Modifier
              .weight(1f)
              .testTag("drive_restore_button"),
          ) {
            Icon(Icons.Rounded.CloudDownload, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.restoreFromDrive)
          }
        }
      }
    }

    // Local Export & Import
    item {
      SettingsCard(title = strings.localExportSection, icon = Icons.Rounded.Download) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = { csvExportLauncher.launch("hisab_expenses.csv") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("export_csv_button"),
          ) {
            Icon(Icons.Rounded.Download, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(strings.exportCsv)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = { jsonExportLauncher.launch("hisab_backup.json") },
              modifier = Modifier
                .weight(1f)
                .testTag("export_json_button"),
            ) {
              Text(strings.exportJson)
            }
            OutlinedButton(
              onClick = { jsonImportLauncher.launch(arrayOf("application/json", "text/plain")) },
              modifier = Modifier
                .weight(1f)
                .testTag("import_json_button"),
            ) {
              Icon(Icons.Rounded.UploadFile, contentDescription = null, Modifier.size(18.dp))
              Spacer(Modifier.width(4.dp))
              Text(strings.importJson)
            }
          }
        }
      }
    }

    // PIN Security
    item {
      SettingsCard(title = strings.securitySection, icon = Icons.Rounded.Lock) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(strings.pinLockToggle, style = MaterialTheme.typography.titleMedium)
            Text(
              strings.pinLockDesc,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Switch(
            checked = settings.pinEnabled,
            onCheckedChange = { checked ->
              if (checked) showPinSetupDialog = true else onDisablePin()
            },
            modifier = Modifier.testTag("pin_lock_switch"),
          )
        }

        if (settings.pinEnabled) {
          Spacer(Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(strings.lockOnBackground, style = MaterialTheme.typography.bodyMedium)
            Switch(
              checked = settings.lockOnBackground,
              onCheckedChange = onToggleLockOnBackground,
            )
          }
          Spacer(Modifier.height(6.dp))
          OutlinedButton(
            onClick = { showPinSetupDialog = true },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(strings.changePin)
          }
        }
      }
    }

    // Appearance & Custom Theme
    item {
      SettingsCard(title = strings.appearanceSection, icon = Icons.Rounded.DarkMode) {
        Text(strings.themeModeLabel, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val modes = listOf(
            ThemeMode.SYSTEM to strings.themeSystem,
            ThemeMode.LIGHT to strings.themeLight,
            ThemeMode.DARK to strings.themeDark,
          )
          modes.forEach { (mode, label) ->
            FilterChip(
              selected = settings.themeMode == mode,
              onClick = { onSelectThemeMode(mode) },
              label = { Text(label) },
              modifier = Modifier.testTag("theme_mode_${mode.id}"),
            )
          }
        }

        Spacer(Modifier.height(14.dp))
        Text(strings.themePaletteLabel, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          AppThemePalette.entries.forEach { palette ->
            val swatch = paletteSwatchColor(palette)
            val selected = settings.themePalette == palette
            val label = when (palette) {
              AppThemePalette.EMERALD -> strings.themeEmerald
              AppThemePalette.INDIGO -> strings.themeIndigo
              AppThemePalette.TERRACOTTA -> strings.themeTerracotta
              AppThemePalette.OCEAN -> strings.themeOcean
              AppThemePalette.PLUM -> strings.themePlum
            }
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectThemePalette(palette) }
                .padding(4.dp)
                .testTag("palette_${palette.id}"),
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(swatch)
                  .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape,
                  ),
                contentAlignment = Alignment.Center,
              ) {
                if (selected) {
                  Icon(
                    Icons.Rounded.Check,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
              Spacer(Modifier.height(4.dp))
              Text(label, style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    // Language & Currency
    item {
      SettingsCard(title = strings.languageCurrencySection, icon = Icons.Rounded.Language) {
        Text(strings.languageLabel, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AppLanguage.entries.forEach { lang ->
            FilterChip(
              selected = settings.language == lang,
              onClick = { onSelectLanguage(lang) },
              label = { Text(lang.displayName) },
              modifier = Modifier.testTag("lang_${lang.code}"),
            )
          }
        }

        Spacer(Modifier.height(12.dp))
        Text(strings.currencyLabel, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CurrencyOption.entries.forEach { option ->
            FilterChip(
              selected = settings.currencyCode == option.code,
              onClick = { onSelectCurrency(option.code) },
              label = { Text(option.label) },
            )
          }
        }
      }
    }

    // Custom Categories Manager
    item {
      SettingsCard(title = strings.customCategoriesSection) {
        customCategories.forEach { cat ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(cat.colorHex).copy(alpha = 0.16f)),
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
            Text(
              text = cat.name,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onDeleteCustomCategory(cat.id) }) {
              Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
            }
          }
        }
        OutlinedButton(
          onClick = { showCustomCatDialog = true },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Icons.Rounded.Add, contentDescription = null, Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text(strings.addCustomCategory)
        }
      }
    }

    // Ad-Free Notice
    item {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Rounded.VerifiedUser,
            contentDescription = strings.adFreeBadge,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
          )
          Spacer(Modifier.width(12.dp))
          Text(
            text = strings.adFreeNotice,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
    }
  }

  if (showPinSetupDialog) {
    SetupPinDialog(
      onDismiss = { showPinSetupDialog = false },
      onPinConfirmed = { newPin ->
        onEnablePin(newPin)
        showPinSetupDialog = false
      },
    )
  }

  if (showCustomCatDialog) {
    AddCustomCategoryDialog(
      onDismiss = { showCustomCatDialog = false },
      onConfirm = { name, iconKey, colorHex ->
        onAddCustomCategory(name, iconKey, colorHex)
        showCustomCatDialog = false
      },
    )
  }

  if (showDriveRestoreDialog) {
    AlertDialog(
      onDismissRequest = { showDriveRestoreDialog = false },
      title = { Text(strings.chooseBackupTitle) },
      text = {
        if (driveFiles.isEmpty()) {
          Text(strings.noDriveBackups)
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            driveFiles.forEach { file ->
              Card(
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    val token = activeDriveToken
                    if (token != null) {
                      onRestoreDriveFile(token, file.id)
                    }
                    showDriveRestoreDialog = false
                  },
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(file.name, style = MaterialTheme.typography.titleMedium)
                  Text(
                    text = "${file.modifiedTime.take(10)} • ${(file.sizeBytes / 1024).coerceAtLeast(1)} KB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showDriveRestoreDialog = false }) {
          Text(strings.cancel)
        }
      },
    )
  }
}

@Composable
private fun SettingsCard(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
  content: @Composable () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ),
    shape = RoundedCornerShape(22.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Spacer(Modifier.width(8.dp))
        }
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
      }
      Spacer(Modifier.height(12.dp))
      content()
    }
  }
}
