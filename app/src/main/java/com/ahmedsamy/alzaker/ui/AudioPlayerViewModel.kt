package com.ahmedsamy.alzaker.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ahmedsamy.alzaker.AlzakerApp
import com.ahmedsamy.alzaker.audio.DhikrAudioService
import kotlinx.coroutines.launch

/**
 * In-app dhikr audio player, mirroring the legacy AudioContext
 * (toggleDhikrSound / playDhikrSound). Playback itself runs in the
 * [DhikrAudioService] foreground media service, so the dhikr behaves like a
 * media player: it keeps playing with the screen locked or the app dismissed,
 * and it shows media controls in the notification shade and on the lock
 * screen. This ViewModel mirrors the service's published state into Compose
 * state so the UI (cards, overlay) stays in sync without owning the player.
 */
class AudioPlayerViewModel(private val appContext: Context) : ViewModel() {

    var currentlyPlayingId by mutableStateOf<Int?>(null)
        private set

    var currentlyPlayingText by mutableStateOf<String?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isPreparing by mutableStateOf(false)
        private set

    init {
        currentlyPlayingId = DhikrAudioService.currentlyPlayingId.value
        currentlyPlayingText = DhikrAudioService.currentlyPlayingText.value
        isPlaying = DhikrAudioService.isPlaying.value
        isPreparing = DhikrAudioService.isPreparing.value
        viewModelScope.launch {
            launch { DhikrAudioService.currentlyPlayingId.collect { currentlyPlayingId = it } }
            launch { DhikrAudioService.currentlyPlayingText.collect { currentlyPlayingText = it } }
            launch { DhikrAudioService.isPlaying.collect { isPlaying = it } }
            launch { DhikrAudioService.isPreparing.collect { isPreparing = it } }
        }
    }

    /** Mirrors legacy playDhikrSound. */
    fun play(id: Int, url: String, text: String, volume: Float = 1f) {
        DhikrAudioService.play(appContext, id, url, text, volume)
    }

    /** Mirrors legacy toggleDhikrSound: the same id toggles the sound off. */
    fun toggle(id: Int, url: String, text: String, volume: Float = 1f) {
        if (DhikrAudioService.currentlyPlayingId.value == id) {
            stop()
        } else {
            play(id, url, text, volume)
        }
    }

    fun pause() {
        DhikrAudioService.pause(appContext)
    }

    fun resume() {
        DhikrAudioService.resume(appContext)
    }

    fun stop() {
        DhikrAudioService.stop(appContext)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlzakerApp
                AudioPlayerViewModel(appContext = app)
            }
        }
    }
}
