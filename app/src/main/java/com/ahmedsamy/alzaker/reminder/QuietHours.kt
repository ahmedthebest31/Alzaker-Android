package com.ahmedsamy.alzaker.reminder

import com.ahmedsamy.alzaker.data.local.AppSettings

/**
 * Pure quiet-hours logic, mirroring the legacy semantics in
 * utils/notifications.ts (parseTimeToMinutes / isQuietTimeNow) and
 * services/AudioDropWorker.ts (parseTime / isQuietTime). No Android imports,
 * so it is fully unit-testable on the JVM.
 */
object QuietHours {

    /**
     * Parses "HH:MM" into minutes-of-day, or -1 when malformed. Mirrors the
     * legacy helper exactly: exactly two ':'-separated parts, each an integer,
     * minutes = hour * 60 + minute. Values outside 0-23 / 0-59 are not range
     * validated (same as the legacy parseInt-based parser).
     */
    fun parseTimeToMinutes(hhmm: String): Int {
        val parts = hhmm.split(':')
        if (parts.size != 2) return -1
        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()
        if (hour == null || minute == null) return -1
        return hour * 60 + minute
    }

    /**
     * True when [currentMinutes] lies inside the inclusive window
     * [startMinutes]..[endMinutes]. Windows that cross midnight (start > end)
     * wrap around, matching the legacy isQuietTime / isQuietTimeNow.
     */
    fun isQuietTime(currentMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean =
        if (startMinutes <= endMinutes) {
            currentMinutes >= startMinutes && currentMinutes <= endMinutes
        } else {
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }

    /**
     * Whether [currentMinutes] is inside the enabled quiet window of
     * [settings]. A disabled flag or a malformed time disables quiet hours,
     * like the legacy isCurrentlyQuiet / isCurrentlyQuietHours.
     */
    fun isQuietNow(currentMinutes: Int, settings: AppSettings): Boolean {
        if (!settings.quietHoursEnabled) return false
        val start = parseTimeToMinutes(settings.quietStart)
        val end = parseTimeToMinutes(settings.quietEnd)
        if (start == -1 || end == -1) return false
        return isQuietTime(currentMinutes, start, end)
    }
}
