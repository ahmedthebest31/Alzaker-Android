package com.ahmedsamy.alzaker.util

/**
 * Pure 12/24 hour conversion helpers mirroring the legacy settings.tsx
 * functions (to12Hour / to24Hour / isValidTime12) used by the quiet-hours UI.
 * Kept free of Android dependencies so they are unit-testable on the JVM.
 */
object TimeFormat {

    enum class Period { AM, PM }

    data class TwelveHourTime(
        val hour: String,
        val minute: String,
        val period: Period,
    )

    /**
     * Converts a 24-hour "HH:MM" string into a 12-hour display value.
     * Falls back to "00" for a missing minute part, like the legacy code.
     */
    fun to12Hour(h24: String): TwelveHourTime {
        val parts = h24.split(':')
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1) ?: "00"
        val period = if (h >= 12) Period.PM else Period.AM
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return TwelveHourTime(hour = h12.toString(), minute = m, period = period)
    }

    /**
     * Converts a 12-hour time into "HH:MM" (24h). Returns null when the hour
     * is not in 1..12 (the legacy function returns "" for the same case).
     */
    fun to24Hour(hour: String, minute: String, period: Period): String? {
        val h = hour.toIntOrNull()
        if (h == null || h < 1 || h > 12) return null
        var h24 = h
        if (period == Period.AM && h == 12) h24 = 0
        if (period == Period.PM && h != 12) h24 += 12
        return h24.toString().padStart(2, '0') + ":" + minute.padStart(2, '0')
    }

    /** True when the 12-hour hour/minute fields are structurally valid. */
    fun isValidTime12(hour: String, minute: String): Boolean {
        val h = hour.toIntOrNull()
        val m = minute.toIntOrNull()
        return h != null && h in 1..12 && m != null && m in 0..59
    }
}
