package com.ahmedsamy.alzaker.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntervalValidationTest {

    @Test
    fun hikmahIntervalOneIsValid() {
        assertTrue(IntervalValidation.isValidHikmahInterval("1"))
    }

    @Test
    fun hikmahIntervalZeroIsInvalid() {
        assertFalse(IntervalValidation.isValidHikmahInterval("0"))
    }

    @Test
    fun hikmahIntervalNonNumericIsInvalid() {
        assertFalse(IntervalValidation.isValidHikmahInterval("abc"))
    }

    @Test
    fun hikmahIntervalEmptyIsInvalid() {
        assertFalse(IntervalValidation.isValidHikmahInterval(""))
    }

    @Test
    fun audioIntervalFifteenIsValid() {
        assertTrue(IntervalValidation.isAudioIntervalValid("15"))
    }

    @Test
    fun audioIntervalFourteenIsInvalid() {
        assertFalse(IntervalValidation.isAudioIntervalValid("14"))
    }

    @Test
    fun audioIntervalNonNumericIsInvalid() {
        assertFalse(IntervalValidation.isAudioIntervalValid("x"))
    }
}
