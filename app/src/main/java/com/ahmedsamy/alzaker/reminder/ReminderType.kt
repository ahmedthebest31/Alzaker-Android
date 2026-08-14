package com.ahmedsamy.alzaker.reminder

import com.ahmedsamy.alzaker.data.local.AppSettings

/**
 * The two reminder chains, mirroring the legacy tadhkir (audible audio drop)
 * and hikmah (silent wisdom notification) reminders in the Expo app.
 */
enum class ReminderType(
    val requestCode: Int,
    val action: String,
    val channelId: String,
    val defaultIntervalMinutes: Int,
) {
    TADHKIR(
        requestCode = 2001,
        action = "com.ahmedsamy.alzaker.action.REMINDER_TADHKIR",
        channelId = NotificationChannels.TADHKIR_CHANNEL_ID,
        defaultIntervalMinutes = AppSettings.DEFAULT_TADHKIR_INTERVAL_MINUTES,
    ),
    HIKMAH(
        requestCode = 2002,
        action = "com.ahmedsamy.alzaker.action.REMINDER_HIKMAH",
        channelId = NotificationChannels.HIKMAH_CHANNEL_ID,
        defaultIntervalMinutes = AppSettings.DEFAULT_HIKMAH_INTERVAL_MINUTES,
    ),
}
