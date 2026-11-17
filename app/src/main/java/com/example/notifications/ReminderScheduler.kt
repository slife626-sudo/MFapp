package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object ReminderScheduler {
  private const val CHANNEL_ID = "hisab_reminders"
  private const val DAILY_REMINDER_REQ_CODE = 1001
  private const val DAILY_NOTIFICATION_ID = 2001
  private const val BUDGET_NOTIFICATION_ID = 2002

  fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Hisab Reminders & Budget Alerts",
        NotificationManager.IMPORTANCE_DEFAULT,
      ).apply {
        description = "Daily expense logging reminders and monthly budget alerts"
      }
      manager.createNotificationChannel(channel)
    }
  }

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun scheduleDailyReminder(
    context: Context,
    enabled: Boolean,
    hour: Int,
    minute: Int,
    langCode: String,
  ) {
    ensureChannel(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java).apply {
      putExtra(ReminderReceiver.EXTRA_LANG_CODE, langCode)
      putExtra(ReminderReceiver.EXTRA_HOUR, hour)
      putExtra(ReminderReceiver.EXTRA_MINUTE, minute)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      DAILY_REMINDER_REQ_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    if (!enabled) {
      alarmManager.cancel(pendingIntent)
      return
    }

    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= System.currentTimeMillis()) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    alarmManager.setInexactRepeating(
      AlarmManager.RTC_WAKEUP,
      target.timeInMillis,
      AlarmManager.INTERVAL_DAY,
      pendingIntent,
    )
  }

  fun showReminderNotification(context: Context, title: String, body: String) {
    postNotification(context, DAILY_NOTIFICATION_ID, title, body)
  }

  fun showBudgetAlertNotification(context: Context, title: String, body: String) {
    postNotification(context, BUDGET_NOTIFICATION_ID, title, body)
  }

  private fun postNotification(
    context: Context,
    notificationId: Int,
    title: String,
    body: String,
  ) {
    if (!hasNotificationPermission(context)) return
    ensureChannel(context)
    val openIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher_round)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)
      .build()

    try {
      NotificationManagerCompat.from(context).notify(notificationId, notification)
    } catch (_: SecurityException) {
      // Permission revoked
    }
  }
}
