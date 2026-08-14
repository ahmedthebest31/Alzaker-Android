package com.ahmedsamy.alzaker.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Notification channels, mirroring the legacy expo-notifications setup in
 * utils/notifications.ts:
 * - hikmah_channel "تذكير حكمة (صامت)": silent wisdom notifications.
 * - tadhkir_channel "تذكير مسموع": audible reminder channel. The audio itself
 *   is played by MediaPlayer; the channel stays silent exactly like the legacy
 *   app (importance DEFAULT, no sound).
 */
object NotificationChannels {

    const val HIKMAH_CHANNEL_ID = "hikmah_channel"
    const val TADHKIR_CHANNEL_ID = "tadhkir_channel"

    const val HIKMAH_NOTIFICATION_ID = 1001

    fun create(context: Context) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val hikmahChannel = NotificationChannel(
            HIKMAH_CHANNEL_ID,
            "تذكير حكمة (صامت)",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            setSound(null, null)
        }

        val tadhkirChannel = NotificationChannel(
            TADHKIR_CHANNEL_ID,
            "تذكير مسموع",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            setSound(null, null)
        }

        manager.createNotificationChannel(hikmahChannel)
        manager.createNotificationChannel(tadhkirChannel)
    }
}
