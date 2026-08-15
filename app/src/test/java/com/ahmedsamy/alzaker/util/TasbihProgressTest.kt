package com.ahmedsamy.alzaker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TasbihProgressTest {

    @Test
    fun goalNotReachedBelowGoal() {
        assertFalse(TasbihProgress.isGoalReached(count = 3, goal = 33))
    }

    @Test
    fun goalReachedExactlyAtGoal() {
        assertTrue(TasbihProgress.isGoalReached(count = 33, goal = 33))
    }

    @Test
    fun goalReachedPastGoal() {
        assertTrue(TasbihProgress.isGoalReached(count = 40, goal = 33))
    }

    @Test
    fun noGoalNeverReached() {
        assertFalse(TasbihProgress.isGoalReached(count = 0, goal = 0))
        assertFalse(TasbihProgress.isGoalReached(count = 100, goal = 0))
    }

    @Test
    fun labelShownWhenGoalSet() {
        assertEquals("3/33", TasbihProgress.progressLabel(count = 3, goal = 33))
    }

    @Test
    fun labelAllowsCountingPastGoal() {
        assertEquals("40/33", TasbihProgress.progressLabel(count = 40, goal = 33))
    }

    @Test
    fun labelNullWithoutGoal() {
        assertNull(TasbihProgress.progressLabel(count = 5, goal = 0))
    }

    @Test
    fun descriptionIncludesGoalWhenSet() {
        assertEquals("العداد: 3 من 33", TasbihProgress.contentDescription(count = 3, goal = 33))
    }

    @Test
    fun descriptionOmitsGoalWhenUnset() {
        assertEquals("العداد: 5", TasbihProgress.contentDescription(count = 5, goal = 0))
    }
}
