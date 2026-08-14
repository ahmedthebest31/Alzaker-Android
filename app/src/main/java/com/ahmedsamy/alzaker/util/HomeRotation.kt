package com.ahmedsamy.alzaker.util

/**
 * Rotation timing for the home-screen dhikr, mirroring the legacy index.tsx
 * rule: reading time is 100 ms per character, clamped between 5 and 7 seconds.
 */
object HomeRotation {

    const val MIN_INTERVAL_MS = 5_000L
    const val MAX_INTERVAL_MS = 7_000L
    const val READING_SPEED_MS_PER_CHAR = 100L

    fun intervalFor(dhikrText: String): Long =
        (dhikrText.length * READING_SPEED_MS_PER_CHAR).coerceIn(MIN_INTERVAL_MS, MAX_INTERVAL_MS)
}
