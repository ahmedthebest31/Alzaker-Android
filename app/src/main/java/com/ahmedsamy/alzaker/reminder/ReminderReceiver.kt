package com.ahmedsamy.alzaker.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ahmedsamy.alzaker.AlzakerApp
import com.ahmedsamy.alzaker.MainActivity
import com.ahmedsamy.alzaker.R
import com.ahmedsamy.alzaker.data.di.AppContainer
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Fires a reminder chain:
 * - HIKMAH: posts a silent wisdom notification with a fresh random dhikr,
 *   mirroring legacy scheduleHikmahNotification (title 'أَذْكُرُ اللهَ').
 * - TADHKIR: starts [AudioReminderService] to play a random audio drop.
 * Both skip the actual fire while quiet hours are active (legacy
 * AudioDropWorker behavior), then re-schedule the next one-shot alarm
 * (self-rescheduling chain, D6).
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        /**
         * The dhikr text carried in the hikmah notification's contentIntent so
         * MainActivity can copy it to the clipboard on tap, mirroring the
         * legacy Notifications.addNotificationResponseReceivedListener
         * behavior in app/(tabs)/settings.tsx.
         */
        const val EXTRA_NOTIFICATION_DHIKR_TEXT = "extra_notification_dhikr_text"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val typeName = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_REMINDER_TYPE)
        val type = typeName?.let { runCatching { ReminderType.valueOf(it) }.getOrNull() }
            ?: return

        // goAsync: the reminder work may read DataStore on an IO thread.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleReminder(context.applicationContext, type, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleReminder(context: Context, type: ReminderType, intent: Intent) {
        val container = (context as AlzakerApp).container
        val settings = container.settingsRepository.settings.first()

        val intervalMinutes = intent.getIntExtra(
            AlarmSchedulerImpl.EXTRA_INTERVAL_MINUTES,
            type.defaultIntervalMinutes,
        )

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        if (!QuietHours.isQuietNow(currentMinutes, settings)) {
            when (type) {
                ReminderType.HIKMAH -> postHikmahNotification(context, container)
                ReminderType.TADHKIR -> startAudioService(context)
            }
        }

        AlarmSchedulerImpl(context).scheduleNext(type, intervalMinutes)
    }

    private fun postHikmahNotification(context: Context, container: AppContainer) {
        val dhikrText = container.dhikrRepository.getRandomDhikr()?.dhikr ?: return
        val contentIntent = PendingIntent.getActivity(
            context,
            NotificationChannels.HIKMAH_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_NOTIFICATION_DHIKR_TEXT, dhikrText),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.HIKMAH_CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("أَذْكُرُ اللهَ")
            .setContentText(dhikrText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(dhikrText))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        NotificationManagerCompat.from(context).notify(NotificationChannels.HIKMAH_NOTIFICATION_ID, notification)
    }

    private fun startAudioService(context: Context) {
        val serviceIntent = Intent(context, AudioReminderService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
