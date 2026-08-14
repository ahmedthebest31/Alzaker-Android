package com.ahmedsamy.alzaker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahmedsamy.alzaker.data.local.AppSettings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed settings repository. Key names mirror the legacy app's
 * AsyncStorage keys so a future migration is straightforward.
 */
class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs -> prefs.toAppSettings() }

    override suspend fun setHasLaunched(value: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.HAS_LAUNCHED] = value }
    }

    override suspend fun setThemeName(name: String) {
        dataStore.edit { prefs -> prefs[Keys.THEME_NAME] = name }
    }

    override suspend fun setFontSizeMultiplier(multiplier: Float) {
        dataStore.edit { prefs -> prefs[Keys.FONT_SIZE_MULTIPLIER] = multiplier }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.HAPTICS_ENABLED] = enabled }
    }

    override suspend fun setAudioVolume(volume: Float) {
        dataStore.edit { prefs -> prefs[Keys.AUDIO_VOLUME] = volume }
    }

    override suspend fun setIsTadhkirEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.TADHKIR_ENABLED] = enabled }
    }

    override suspend fun setTadhkirIntervalMinutes(minutes: Int) {
        dataStore.edit { prefs -> prefs[Keys.TADHKIR_INTERVAL] = minutes }
    }

    override suspend fun setIsHikmahEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.HIKMAH_ENABLED] = enabled }
    }

    override suspend fun setHikmahIntervalMinutes(minutes: Int) {
        dataStore.edit { prefs -> prefs[Keys.HIKMAH_INTERVAL] = minutes }
    }

    override suspend fun setQuietHoursEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.QUIET_HOURS_ENABLED] = enabled }
    }

    override suspend fun setQuietStart(hhmm: String) {
        dataStore.edit { prefs -> prefs[Keys.QUIET_START] = hhmm }
    }

    override suspend fun setQuietEnd(hhmm: String) {
        dataStore.edit { prefs -> prefs[Keys.QUIET_END] = hhmm }
    }

    private fun Preferences.toAppSettings(): AppSettings = AppSettings(
        hasLaunched = this[Keys.HAS_LAUNCHED] ?: false,
        themeName = this[Keys.THEME_NAME] ?: AppSettings.DEFAULT_THEME_NAME,
        fontSizeMultiplier = this[Keys.FONT_SIZE_MULTIPLIER] ?: AppSettings.DEFAULT_FONT_SIZE_MULTIPLIER,
        hapticsEnabled = this[Keys.HAPTICS_ENABLED] ?: AppSettings.DEFAULT_HAPTICS_ENABLED,
        audioVolume = this[Keys.AUDIO_VOLUME] ?: AppSettings.DEFAULT_AUDIO_VOLUME,
        isTadhkirEnabled = this[Keys.TADHKIR_ENABLED] ?: false,
        tadhkirIntervalMinutes = this[Keys.TADHKIR_INTERVAL] ?: AppSettings.DEFAULT_TADHKIR_INTERVAL_MINUTES,
        isHikmahEnabled = this[Keys.HIKMAH_ENABLED] ?: false,
        hikmahIntervalMinutes = this[Keys.HIKMAH_INTERVAL] ?: AppSettings.DEFAULT_HIKMAH_INTERVAL_MINUTES,
        quietHoursEnabled = this[Keys.QUIET_HOURS_ENABLED] ?: false,
        quietStart = this[Keys.QUIET_START] ?: AppSettings.DEFAULT_QUIET_START,
        quietEnd = this[Keys.QUIET_END] ?: AppSettings.DEFAULT_QUIET_END,
    )

    private companion object Keys {
        val HAS_LAUNCHED = booleanPreferencesKey("has_launched")
        val THEME_NAME = stringPreferencesKey("theme_name")
        val FONT_SIZE_MULTIPLIER = floatPreferencesKey("font_size_multiplier")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val AUDIO_VOLUME = floatPreferencesKey("audio_volume")
        val TADHKIR_ENABLED = booleanPreferencesKey("tadhkir_enabled")
        val TADHKIR_INTERVAL = intPreferencesKey("tadhkir_interval")
        val HIKMAH_ENABLED = booleanPreferencesKey("hikmah_enabled")
        val HIKMAH_INTERVAL = intPreferencesKey("hikmah_interval")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_START = stringPreferencesKey("quiet_start")
        val QUIET_END = stringPreferencesKey("quiet_end")
    }
}
