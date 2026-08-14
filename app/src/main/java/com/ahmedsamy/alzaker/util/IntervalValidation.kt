package com.ahmedsamy.alzaker.util

/**
 * Interval validation mirroring the legacy settings.tsx checks:
 * the silent hikmah reminder accepts any whole minute >= 1, while the audible
 * tadhkir reminder requires >= [MIN_AUDIO_INTERVAL] because background audio
 * playback is system-restricted below that threshold.
 */
object IntervalValidation {

    const val MIN_AUDIO_INTERVAL = 15

    fun isValidHikmahInterval(value: String): Boolean {
        val minutes = value.toIntOrNull() ?: return false
        return minutes >= 1
    }

    fun isAudioIntervalValid(value: String): Boolean {
        val minutes = value.toIntOrNull() ?: return false
        return minutes >= MIN_AUDIO_INTERVAL
    }
}
