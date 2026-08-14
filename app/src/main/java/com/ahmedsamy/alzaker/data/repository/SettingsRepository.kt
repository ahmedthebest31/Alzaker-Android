package com.ahmedsamy.alzaker.data.repository

import com.ahmedsamy.alzaker.data.local.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Persists and exposes all user settings as a single AppSettings snapshot.
 */
interface SettingsRepository {

    val settings: Flow<AppSettings>

    /** Marks the onboarding wizard as completed (legacy 'hasLaunched' key). */
    suspend fun setHasLaunched(value: Boolean)

    suspend fun setThemeName(name: String)

    suspend fun setFontSizeMultiplier(multiplier: Float)

    suspend fun setHapticsEnabled(enabled: Boolean)

    suspend fun setAudioVolume(volume: Float)

    suspend fun setIsTadhkirEnabled(enabled: Boolean)

    suspend fun setTadhkirIntervalMinutes(minutes: Int)

    suspend fun setIsHikmahEnabled(enabled: Boolean)

    suspend fun setHikmahIntervalMinutes(minutes: Int)

    suspend fun setQuietHoursEnabled(enabled: Boolean)

    suspend fun setQuietStart(hhmm: String)

    suspend fun setQuietEnd(hhmm: String)
}
