package com.ahmedsamy.alzaker.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * AlarmManager-backed scheduler using exact one-shot alarms
 * (setExactAndAllowWhileIdle) so reminders fire reliably in Doze. When the
 * USE_EXACT_ALARM permission is not granted (canScheduleExactAlarms() ==
 * false), gracefully falls back to setAndAllowWhileIdle (note N2).
 *
 * The requested interval is carried in the PendingIntent extras so the fired
 * receiver can re-schedule the next alarm with the same cadence.
 */
class AlarmSchedulerImpl(context: Context) : AlarmScheduler {

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleNext(type: ReminderType, intervalMinutes: Int) {
        val safeInterval = if (intervalMinutes < 1) type.defaultIntervalMinutes else intervalMinutes
        val triggerAt = System.currentTimeMillis() + safeInterval * MINUTE_MILLIS
        val pendingIntent = buildPendingIntent(type, safeInterval)

        val canScheduleExact =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            Log.w(TAG, "Exact alarms not permitted; using inexact setAndAllowWhileIdle for $type")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    override fun cancel(type: ReminderType) {
        alarmManager.cancel(buildPendingIntent(type, 0))
    }

    private fun buildPendingIntent(type: ReminderType, intervalMinutes: Int): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = type.action
            putExtra(EXTRA_REMINDER_TYPE, type.name)
            putExtra(EXTRA_INTERVAL_MINUTES, intervalMinutes)
        }
        return PendingIntent.getBroadcast(
            appContext,
            type.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val TAG = "AlarmScheduler"
        private const val MINUTE_MILLIS = 60_000L

        const val EXTRA_REMINDER_TYPE = "reminder_type"
        const val EXTRA_INTERVAL_MINUTES = "interval_minutes"
    }
}
