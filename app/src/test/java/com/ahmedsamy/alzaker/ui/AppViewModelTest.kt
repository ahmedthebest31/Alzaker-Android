package com.ahmedsamy.alzaker.ui

import com.ahmedsamy.alzaker.data.local.AppSettings
import com.ahmedsamy.alzaker.data.repository.DhikrRepositoryImpl
import com.ahmedsamy.alzaker.data.repository.FavoritesRepository
import com.ahmedsamy.alzaker.data.repository.SettingsRepository
import com.ahmedsamy.alzaker.reminder.AlarmScheduler
import com.ahmedsamy.alzaker.reminder.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    val state = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = state

    override suspend fun setHasLaunched(value: Boolean) = state.update { it.copy(hasLaunched = value) }
    override suspend fun setThemeName(name: String) = state.update { it.copy(themeName = name) }
    override suspend fun setFontSizeMultiplier(multiplier: Float) = state.update { it.copy(fontSizeMultiplier = multiplier) }
    override suspend fun setHapticsEnabled(enabled: Boolean) = state.update { it.copy(hapticsEnabled = enabled) }
    override suspend fun setAudioVolume(volume: Float) = state.update { it.copy(audioVolume = volume) }
    override suspend fun setIsTadhkirEnabled(enabled: Boolean) = state.update { it.copy(isTadhkirEnabled = enabled) }
    override suspend fun setTadhkirIntervalMinutes(minutes: Int) = state.update { it.copy(tadhkirIntervalMinutes = minutes) }
    override suspend fun setIsHikmahEnabled(enabled: Boolean) = state.update { it.copy(isHikmahEnabled = enabled) }
    override suspend fun setHikmahIntervalMinutes(minutes: Int) = state.update { it.copy(hikmahIntervalMinutes = minutes) }
    override suspend fun setQuietHoursEnabled(enabled: Boolean) = state.update { it.copy(quietHoursEnabled = enabled) }
    override suspend fun setQuietStart(hhmm: String) = state.update { it.copy(quietStart = hhmm) }
    override suspend fun setQuietEnd(hhmm: String) = state.update { it.copy(quietEnd = hhmm) }
}

private class FakeFavoritesRepository : FavoritesRepository {
    private val state = MutableStateFlow(emptySet<Int>())
    override val favoriteIds: Flow<Set<Int>> = state

    override suspend fun isFavorite(id: Int): Boolean = state.value.contains(id)

    override suspend fun toggleFavorite(id: Int): Boolean {
        val next = if (id in state.value) state.value - id else state.value + id
        state.value = next
        return id in next
    }

    override suspend fun addFavorite(id: Int) {
        state.value = state.value + id
    }

    override suspend fun removeFavorite(id: Int) {
        state.value = state.value - id
    }
}

private class RecordingAlarmScheduler : AlarmScheduler {
    val scheduled = mutableListOf<Pair<ReminderType, Int>>()
    val cancelled = mutableListOf<ReminderType>()

    override fun scheduleNext(type: ReminderType, intervalMinutes: Int) {
        scheduled.add(type to intervalMinutes)
    }

    override fun cancel(type: ReminderType) {
        cancelled.add(type)
    }
}

class AppViewModelTest {

    private fun newViewModel(
        scope: CoroutineScope,
        settings: FakeSettingsRepository = FakeSettingsRepository(),
        favorites: FakeFavoritesRepository = FakeFavoritesRepository(),
        scheduler: RecordingAlarmScheduler = RecordingAlarmScheduler(),
    ) = AppViewModel(
        settingsRepository = settings,
        favoritesRepository = favorites,
        dhikrRepository = DhikrRepositoryImpl(emptyList(), emptyList()),
        scheduler = scheduler,
        scope = scope,
        sharingStarted = SharingStarted.Lazily,
    ).let { Triple(it, settings, scheduler) }

    @Test
    fun enablingTadhkirSchedulesAlarm() = runBlocking {
        val (vm, settings, scheduler) = newViewModel(scope = this)
        vm.setTadhkirEnabled(true, 15).join()
        assertEquals(listOf(ReminderType.TADHKIR to 15), scheduler.scheduled)
        assertTrue(settings.state.value.isTadhkirEnabled)
    }

    @Test
    fun disablingTadhkirCancelsAlarm() = runBlocking {
        val (vm, settings, scheduler) = newViewModel(scope = this)
        vm.setTadhkirEnabled(false, 15).join()
        assertEquals(listOf(ReminderType.TADHKIR), scheduler.cancelled)
        assertFalse(settings.state.value.isTadhkirEnabled)
    }

    @Test
    fun changingIntervalWhileEnabledReschedules() = runBlocking {
        val (vm, settings, scheduler) = newViewModel(
            scope = this,
            settings = FakeSettingsRepository(AppSettings(isTadhkirEnabled = true)),
        )
        vm.setTadhkirIntervalMinutes(30).join()
        assertEquals(30, settings.state.value.tadhkirIntervalMinutes)
        assertEquals(listOf(ReminderType.TADHKIR to 30), scheduler.scheduled)
    }

    @Test
    fun changingIntervalWhileDisabledDoesNotSchedule() = runBlocking {
        val (vm, settings, scheduler) = newViewModel(scope = this)
        vm.setTadhkirIntervalMinutes(30).join()
        assertEquals(30, settings.state.value.tadhkirIntervalMinutes)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun enablingHikmahSchedulesAlarm() = runBlocking {
        val (vm, settings, scheduler) = newViewModel(scope = this)
        vm.setHikmahEnabled(true, 5).join()
        assertEquals(listOf(ReminderType.HIKMAH to 5), scheduler.scheduled)
        assertTrue(settings.state.value.isHikmahEnabled)
    }

    @Test
    fun changingHikmahIntervalWhileEnabledReschedules() = runBlocking {
        val (vm, settings, scheduler) = newViewModel(
            scope = this,
            settings = FakeSettingsRepository(AppSettings(isHikmahEnabled = true)),
        )
        vm.setHikmahIntervalMinutes(10).join()
        assertEquals(10, settings.state.value.hikmahIntervalMinutes)
        assertEquals(listOf(ReminderType.HIKMAH to 10), scheduler.scheduled)
    }

    @Test
    fun markOnboardingCompletePersistsHasLaunched() = runBlocking {
        val (vm, settings, _) = newViewModel(scope = this)
        vm.markOnboardingComplete().join()
        assertTrue(settings.state.value.hasLaunched)
    }

    @Test
    fun settingThemePersists() = runBlocking {
        val (vm, settings, _) = newViewModel(scope = this)
        vm.setThemeName("dark").join()
        assertEquals("dark", settings.state.value.themeName)
    }

    @Test
    fun settingQuietHoursPersists() = runBlocking {
        val (vm, settings, _) = newViewModel(scope = this)
        vm.setQuietHoursEnabled(true).join()
        vm.setQuietStart("23:30").join()
        vm.setQuietEnd("05:30").join()
        assertTrue(settings.state.value.quietHoursEnabled)
        assertEquals("23:30", settings.state.value.quietStart)
        assertEquals("05:30", settings.state.value.quietEnd)
    }

    @Test
    fun toggleFavoriteAddsThenRemoves() = runBlocking {
        val favorites = FakeFavoritesRepository()
        val vm = AppViewModel(
            settingsRepository = FakeSettingsRepository(),
            favoritesRepository = favorites,
            dhikrRepository = DhikrRepositoryImpl(emptyList(), emptyList()),
            scheduler = RecordingAlarmScheduler(),
            scope = this,
            sharingStarted = SharingStarted.Lazily,
        )

        var result = false
        vm.toggleFavorite(5) { result = it }.join()
        assertTrue(result)
        assertTrue(favorites.isFavorite(5))

        vm.toggleFavorite(5) { result = it }.join()
        assertFalse(result)
        assertFalse(favorites.isFavorite(5))
    }
}
