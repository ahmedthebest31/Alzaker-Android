package com.ahmedsamy.alzaker.util

import com.ahmedsamy.alzaker.util.TimeFormat.Period
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeFormatTest {

    @Test
    fun to12HourMidnightIsTwelveAm() {
        val t = TimeFormat.to12Hour("00:00")
        assertEquals("12", t.hour)
        assertEquals("00", t.minute)
        assertEquals(Period.AM, t.period)
    }

    @Test
    fun to12HourMorningIsAm() {
        val t = TimeFormat.to12Hour("09:15")
        assertEquals("9", t.hour)
        assertEquals("15", t.minute)
        assertEquals(Period.AM, t.period)
    }

    @Test
    fun to12HourNoonIsTwelvePm() {
        val t = TimeFormat.to12Hour("12:00")
        assertEquals("12", t.hour)
        assertEquals(Period.PM, t.period)
    }

    @Test
    fun to12HourAfternoonSubtractsTwelve() {
        val t = TimeFormat.to12Hour("22:00")
        assertEquals("10", t.hour)
        assertEquals(Period.PM, t.period)
    }

    @Test
    fun to12HourDefaultsMissingMinuteToZero() {
        val t = TimeFormat.to12Hour("22")
        assertEquals("00", t.minute)
        assertEquals(Period.PM, t.period)
    }

    @Test
    fun to24HourTenPmIsTwentyTwo() {
        assertEquals("22:00", TimeFormat.to24Hour("10", "00", Period.PM))
    }

    @Test
    fun to24HourSixAmIsSix() {
        assertEquals("06:00", TimeFormat.to24Hour("6", "00", Period.AM))
    }

    @Test
    fun to24HourTwelveAmIsZero() {
        assertEquals("00:30", TimeFormat.to24Hour("12", "30", Period.AM))
    }

    @Test
    fun to24HourTwelvePmStaysTwelve() {
        assertEquals("12:15", TimeFormat.to24Hour("12", "15", Period.PM))
    }

    @Test
    fun to24HourPadsSingleDigitMinute() {
        assertEquals("07:05", TimeFormat.to24Hour("7", "5", Period.AM))
    }

    @Test
    fun to24HourRejectsHourZero() {
        assertNull(TimeFormat.to24Hour("0", "00", Period.AM))
    }

    @Test
    fun to24HourRejectsHourAboveTwelve() {
        assertNull(TimeFormat.to24Hour("13", "00", Period.PM))
    }

    @Test
    fun to24HourRejectsNonNumericHour() {
        assertNull(TimeFormat.to24Hour("ab", "00", Period.AM))
    }

    @Test
    fun isValidTime12AcceptsBoundaries() {
        assertTrue(TimeFormat.isValidTime12("1", "0"))
        assertTrue(TimeFormat.isValidTime12("12", "59"))
    }

    @Test
    fun isValidTime12RejectsOutOfRange() {
        assertFalse(TimeFormat.isValidTime12("0", "00"))
        assertFalse(TimeFormat.isValidTime12("13", "00"))
        assertFalse(TimeFormat.isValidTime12("10", "60"))
        assertFalse(TimeFormat.isValidTime12("", "00"))
        assertFalse(TimeFormat.isValidTime12("10", "ab"))
    }
}
