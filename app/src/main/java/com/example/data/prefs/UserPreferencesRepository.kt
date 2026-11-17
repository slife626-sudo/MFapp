package com.example.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.CurrencyOption
import com.example.i18n.AppLanguage
import com.example.ui.theme.AppThemePalette
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.hisabDataStore by preferencesDataStore(name = "hisab_preferences")

data class UserSettings(
  val language: AppLanguage = AppLanguage.BENGALI,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val themePalette: AppThemePalette = AppThemePalette.EMERALD,
  val currencyCode: String = CurrencyOption.BDT.code,
  val cloudSyncEnabled: Boolean = true,
  val pinEnabled: Boolean = false,
  val pinHash: String = "",
  val lockOnBackground: Boolean = true,
  val dailyReminderEnabled: Boolean = false,
  val reminderHour: Int = 21,
  val reminderMinute: Int = 0,
  val budgetAlertsEnabled: Boolean = true,
  val lastDriveBackupMillis: Long = 0L,
)

class UserPreferencesRepository(private val context: Context) {

  private object Keys {
    val LANGUAGE = stringPreferencesKey("language")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val THEME_PALETTE = stringPreferencesKey("theme_palette")
    val CURRENCY_CODE = stringPreferencesKey("currency_code")
    val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
    val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
    val PIN_HASH = stringPreferencesKey("pin_hash")
    val LOCK_ON_BACKGROUND = booleanPreferencesKey("lock_on_background")
    val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    val BUDGET_ALERTS_ENABLED = booleanPreferencesKey("budget_alerts_enabled")
    val LAST_DRIVE_BACKUP_MILLIS = longPreferencesKey("last_drive_backup_millis")
  }

  val settingsFlow: Flow<UserSettings> = context.hisabDataStore.data.map { prefs ->
    UserSettings(
      language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
      themeMode = ThemeMode.fromId(prefs[Keys.THEME_MODE]),
      themePalette = AppThemePalette.fromId(prefs[Keys.THEME_PALETTE]),
      currencyCode = prefs[Keys.CURRENCY_CODE] ?: CurrencyOption.BDT.code,
      cloudSyncEnabled = prefs[Keys.CLOUD_SYNC_ENABLED] ?: true,
      pinEnabled = prefs[Keys.PIN_ENABLED] ?: false,
      pinHash = prefs[Keys.PIN_HASH] ?: "",
      lockOnBackground = prefs[Keys.LOCK_ON_BACKGROUND] ?: true,
      dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: false,
      reminderHour = prefs[Keys.REMINDER_HOUR] ?: 21,
      reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0,
      budgetAlertsEnabled = prefs[Keys.BUDGET_ALERTS_ENABLED] ?: true,
      lastDriveBackupMillis = prefs[Keys.LAST_DRIVE_BACKUP_MILLIS] ?: 0L,
    )
  }

  suspend fun setLanguage(language: AppLanguage) {
    context.hisabDataStore.edit { it[Keys.LANGUAGE] = language.code }
  }

  suspend fun setThemeMode(mode: ThemeMode) {
    context.hisabDataStore.edit { it[Keys.THEME_MODE] = mode.id }
  }

  suspend fun setThemePalette(palette: AppThemePalette) {
    context.hisabDataStore.edit { it[Keys.THEME_PALETTE] = palette.id }
  }

  suspend fun setCurrencyCode(code: String) {
    context.hisabDataStore.edit { it[Keys.CURRENCY_CODE] = code }
  }

  suspend fun setCloudSyncEnabled(enabled: Boolean) {
    context.hisabDataStore.edit { it[Keys.CLOUD_SYNC_ENABLED] = enabled }
  }

  suspend fun enablePin(rawPin: String) {
    val hash = hashPin(rawPin)
    context.hisabDataStore.edit {
      it[Keys.PIN_HASH] = hash
      it[Keys.PIN_ENABLED] = true
    }
  }

  suspend fun disablePin() {
    context.hisabDataStore.edit {
      it[Keys.PIN_ENABLED] = false
      it[Keys.PIN_HASH] = ""
    }
  }

  suspend fun setLockOnBackground(enabled: Boolean) {
    context.hisabDataStore.edit { it[Keys.LOCK_ON_BACKGROUND] = enabled }
  }

  suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
    context.hisabDataStore.edit {
      it[Keys.DAILY_REMINDER_ENABLED] = enabled
      it[Keys.REMINDER_HOUR] = hour
      it[Keys.REMINDER_MINUTE] = minute
    }
  }

  suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
    context.hisabDataStore.edit { it[Keys.BUDGET_ALERTS_ENABLED] = enabled }
  }

  suspend fun setLastDriveBackupMillis(millis: Long) {
    context.hisabDataStore.edit { it[Keys.LAST_DRIVE_BACKUP_MILLIS] = millis }
  }

  fun verifyPin(rawPin: String, storedHash: String): Boolean {
    if (storedHash.isEmpty()) return false
    return hashPin(rawPin) == storedHash
  }

  private fun hashPin(rawPin: String): String {
    val salted = "hisab_pin_salt_v1:$rawPin"
    val digest = MessageDigest.getInstance("SHA-256").digest(salted.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
  }
}
