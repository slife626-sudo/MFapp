package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.i18n.AppLanguage
import com.example.i18n.stringsFor

class ReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val langCode = intent.getStringExtra(EXTRA_LANG_CODE)
    val hour = intent.getIntExtra(EXTRA_HOUR, 21)
    val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
    val strings = stringsFor(AppLanguage.fromCode(langCode))
    ReminderScheduler.showReminderNotification(
      context = context,
      title = strings.reminderNotificationTitle,
      body = strings.reminderNotificationBody,
    )
    // Reschedule for next day
    ReminderScheduler.scheduleDailyReminder(
      context = context,
      enabled = true,
      hour = hour,
      minute = minute,
      langCode = langCode ?: AppLanguage.BENGALI.code,
    )
  }

  companion object {
    const val EXTRA_LANG_CODE = "extra_lang_code"
    const val EXTRA_HOUR = "extra_hour"
    const val EXTRA_MINUTE = "extra_minute"
  }
}
