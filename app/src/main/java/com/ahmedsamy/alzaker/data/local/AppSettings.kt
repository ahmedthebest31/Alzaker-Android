package com.ahmedsamy.alzaker.data.local

/**
 * Immutable snapshot of all persisted app settings. Defaults match the legacy
 * Expo app's initial values.
 */
data class AppSettings(
    val themeName: String = DEFAULT_THEME_NAME,
    val fontSizeMultiplier: Float = DEFAULT_FONT_SIZE_MULTIPLIER,
    val hapticsEnabled: Boolean = DEFAULT_HAPTICS_ENABLED,
    val audioVolume: Float = DEFAULT_AUDIO_VOLUME,
    val isTadhkirEnabled: Boolean = false,
    val tadhkirIntervalMinutes: Int = DEFAULT_TADHKIR_INTERVAL_MINUTES,
    val isHikmahEnabled: Boolean = false,
    val hikmahIntervalMinutes: Int = DEFAULT_HIKMAH_INTERVAL_MINUTES,
    val quietHoursEnabled: Boolean = false,
    val quietStart: String = DEFAULT_QUIET_START,
    val quietEnd: String = DEFAULT_QUIET_END,
) {
    companion object {
        const val DEFAULT_THEME_NAME = "default"
        const val DEFAULT_FONT_SIZE_MULTIPLIER = 1.0f
        const val DEFAULT_HAPTICS_ENABLED = true
        const val DEFAULT_AUDIO_VOLUME = 1.0f
        const val DEFAULT_TADHKIR_INTERVAL_MINUTES = 15
        const val DEFAULT_HIKMAH_INTERVAL_MINUTES = 5
        const val DEFAULT_QUIET_START = "22:00"
        const val DEFAULT_QUIET_END = "06:00"
    }
}
