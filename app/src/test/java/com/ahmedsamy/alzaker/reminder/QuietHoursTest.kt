package com.ahmedsamy.alzaker.reminder

import com.ahmedsamy.alzaker.data.local.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursTest {

    @Test
    fun parseTime_validFormats() {
        assertEquals(0, QuietHours.parseTimeToMinutes("00:00"))
        assertEquals(60, QuietHours.parseTimeToMinutes("01:00"))
        assertEquals(605, QuietHours.parseTimeToMinutes("10:5"))
        assertEquals(1320, QuietHours.parseTimeToMinutes("22:00"))
        assertEquals(360, QuietHours.parseTimeToMinutes("06:00"))
    }

    @Test
    fun parseTime_invalidReturnsMinusOne() {
        assertEquals(-1, QuietHours.parseTimeToMinutes(""))
        assertEquals(-1, QuietHours.parseTimeToMinutes("10"))
        assertEquals(-1, QuietHours.parseTimeToMinutes("10:00:00"))
        assertEquals(-1, QuietHours.parseTimeToMinutes(":"))
        assertEquals(-1, QuietHours.parseTimeToMinutes("ab:cd"))
        assertEquals(-1, QuietHours.parseTimeToMinutes("10:0x"))
    }

    @Test
    fun isQuietTime_sameDayWindowIsInclusive() {
        val start = 6 * 60
        val end = 10 * 60
        assertTrue(QuietHours.isQuietTime(start, start, end))
        assertTrue(QuietHours.isQuietTime(8 * 60, start, end))
        assertTrue(QuietHours.isQuietTime(end, start, end))
        assertFalse(QuietHours.isQuietTime(5 * 60, start, end))
        assertFalse(QuietHours.isQuietTime(end + 1, start, end))
    }

    @Test
    fun isQuietTime_overnightWindowWrapsAround() {
        val start = 22 * 60
        val end = 6 * 60
        assertTrue(QuietHours.isQuietTime(start, start, end))
        assertTrue(QuietHours.isQuietTime(23 * 60, start, end))
        assertTrue(QuietHours.isQuietTime(2 * 60, start, end))
        assertTrue(QuietHours.isQuietTime(end, start, end))
        assertFalse(QuietHours.isQuietTime(end + 1, start, end))
        assertFalse(QuietHours.isQuietTime(21 * 60, start, end))
        assertFalse(QuietHours.isQuietTime(12 * 60, start, end))
    }

    @Test
    fun isQuietTime_zeroLengthWindowMatchesOnlyThatMinute() {
        assertTrue(QuietHours.isQuietTime(90, 90, 90))
        assertFalse(QuietHours.isQuietTime(91, 90, 90))
    }

    @Test
    fun isQuietNow_disabledOrMalformedSettingsNeverQuiet() {
        val disabled = AppSettings(quietHoursEnabled = false, quietStart = "22:00", quietEnd = "06:00")
        assertFalse(QuietHours.isQuietNow(2 * 60, disabled))

        val malformedStart = disabled.copy(quietHoursEnabled = true, quietStart = "bad")
        assertFalse(QuietHours.isQuietNow(2 * 60, malformedStart))

        val malformedEnd = disabled.copy(quietHoursEnabled = true, quietEnd = "")
        assertFalse(QuietHours.isQuietNow(2 * 60, malformedEnd))
    }

    @Test
    fun isQuietNow_enabledOvernightWindow() {
        val settings = AppSettings(quietHoursEnabled = true, quietStart = "22:00", quietEnd = "06:00")
        assertTrue(QuietHours.isQuietNow(22 * 60, settings))
        assertTrue(QuietHours.isQuietNow(3 * 60, settings))
        assertTrue(QuietHours.isQuietNow(6 * 60, settings))
        assertFalse(QuietHours.isQuietNow(12 * 60, settings))
    }
}
