package com.ahmedsamy.alzaker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ahmedsamy.alzaker.AlzakerApp
import com.ahmedsamy.alzaker.data.local.AppSettings
import com.ahmedsamy.alzaker.data.repository.DhikrRepository
import com.ahmedsamy.alzaker.data.repository.FavoritesRepository
import com.ahmedsamy.alzaker.data.repository.SettingsRepository
import com.ahmedsamy.alzaker.reminder.AlarmScheduler
import com.ahmedsamy.alzaker.reminder.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Activity-scoped view model exposing the app-wide state (settings, favorites,
 * dhikr content) and the reminder scheduling side effects, so every screen
 * shares one source of truth.
 *
 * [scope] and [sharingStarted] are injectable so the reminder policy is
 * testable on the JVM. When [scope] is null the real [viewModelScope] is used
 * (production), and it is only touched lazily when state flows are first read.
 */
class AppViewModel(
    private val settingsRepository: SettingsRepository,
    private val favoritesRepository: FavoritesRepository,
    val dhikrRepository: DhikrRepository,
    private val scheduler: AlarmScheduler,
    private val scope: CoroutineScope? = null,
    private val sharingStarted: SharingStarted = SharingStarted.Eagerly,
) : ViewModel() {

    private val effectiveScope: CoroutineScope
        get() = scope ?: viewModelScope

    val settings: StateFlow<AppSettings> by lazy {
        settingsRepository.settings.stateIn(effectiveScope, sharingStarted, AppSettings())
    }

    val favoriteIds: StateFlow<Set<Int>> by lazy {
        favoritesRepository.favoriteIds.stateIn(effectiveScope, sharingStarted, emptySet())
    }

    fun markOnboardingComplete(): Job =
        effectiveScope.launch { settingsRepository.setHasLaunched(true) }

    fun setThemeName(name: String): Job = effectiveScope.launch { settingsRepository.setThemeName(name) }

    fun setFontSizeMultiplier(multiplier: Float): Job =
        effectiveScope.launch { settingsRepository.setFontSizeMultiplier(multiplier) }

    fun setHapticsEnabled(enabled: Boolean): Job =
        effectiveScope.launch { settingsRepository.setHapticsEnabled(enabled) }

    fun setAudioVolume(volume: Float): Job =
        effectiveScope.launch { settingsRepository.setAudioVolume(volume) }

    fun setQuietHoursEnabled(enabled: Boolean): Job =
        effectiveScope.launch { settingsRepository.setQuietHoursEnabled(enabled) }

    fun setQuietStart(hhmm: String): Job = effectiveScope.launch { settingsRepository.setQuietStart(hhmm) }

    fun setQuietEnd(hhmm: String): Job = effectiveScope.launch { settingsRepository.setQuietEnd(hhmm) }

    fun setTadhkirEnabled(enabled: Boolean, intervalMinutes: Int): Job =
        effectiveScope.launch {
            if (enabled) scheduler.scheduleNext(ReminderType.TADHKIR, intervalMinutes)
            else scheduler.cancel(ReminderType.TADHKIR)
            settingsRepository.setIsTadhkirEnabled(enabled)
        }

    fun setTadhkirIntervalMinutes(minutes: Int): Job =
        effectiveScope.launch {
            settingsRepository.setTadhkirIntervalMinutes(minutes)
            if (settingsRepository.settings.first().isTadhkirEnabled) {
                scheduler.scheduleNext(ReminderType.TADHKIR, minutes)
            }
        }

    fun setHikmahEnabled(enabled: Boolean, intervalMinutes: Int): Job =
        effectiveScope.launch {
            if (enabled) scheduler.scheduleNext(ReminderType.HIKMAH, intervalMinutes)
            else scheduler.cancel(ReminderType.HIKMAH)
            settingsRepository.setIsHikmahEnabled(enabled)
        }

    fun setHikmahIntervalMinutes(minutes: Int): Job =
        effectiveScope.launch {
            settingsRepository.setHikmahIntervalMinutes(minutes)
            if (settingsRepository.settings.first().isHikmahEnabled) {
                scheduler.scheduleNext(ReminderType.HIKMAH, minutes)
            }
        }

    /** Toggles a favorite and reports the new state (true = now favorite). */
    fun toggleFavorite(id: Int, onResult: (Boolean) -> Unit): Job =
        effectiveScope.launch {
            val nowFavorite = favoritesRepository.toggleFavorite(id)
            onResult(nowFavorite)
        }

    fun isFavorite(id: Int): Boolean = favoriteIds.value.contains(id)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlzakerApp
                val container = app.container
                AppViewModel(
                    settingsRepository = container.settingsRepository,
                    favoritesRepository = container.favoritesRepository,
                    dhikrRepository = container.dhikrRepository,
                    scheduler = container.alarmScheduler,
                )
            }
        }
    }
}
