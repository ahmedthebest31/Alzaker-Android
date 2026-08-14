package com.ahmedsamy.alzaker.util

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeRotationTest {

    @Test
    fun shortTextClampsToFiveSeconds() {
        assertEquals(5_000L, HomeRotation.intervalFor("سبحان الله"))
    }

    @Test
    fun midLengthTextScalesWithCharacters() {
        val text = "a".repeat(60)
        assertEquals(6_000L, HomeRotation.intervalFor(text))
    }

    @Test
    fun longTextClampsToSevenSeconds() {
        val text = "a".repeat(500)
        assertEquals(7_000L, HomeRotation.intervalFor(text))
    }

    @Test
    fun exactMinimumBoundaryIsKept() {
        assertEquals(5_000L, HomeRotation.intervalFor("a".repeat(50)))
    }

    @Test
    fun exactMaximumBoundaryIsKept() {
        assertEquals(7_000L, HomeRotation.intervalFor("a".repeat(70)))
    }

    @Test
    fun emptyTextClampsToFiveSeconds() {
        assertEquals(5_000L, HomeRotation.intervalFor(""))
    }
}
