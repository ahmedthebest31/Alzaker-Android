package com.ahmedsamy.alzaker.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ahmedsamy.alzaker.AlzakerApp
import com.ahmedsamy.alzaker.reminder.AudioFocusManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * In-app dhikr audio player, mirroring the legacy AudioContext: one MediaPlayer
 * streaming the dhikr's remote audio_url (decision D5) with 'doNotMix' audio
 * focus and a lock-screen media session (title = dhikr text, artist =
 * 'الذاكر'). The user's audio_volume is applied. Playback stops on completion,
 * focus loss, or [stop]. [toggle] mirrors the legacy toggleDhikrSound (same id
 * toggles off).
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

    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null
    private var audioFocusManager: AudioFocusManager? = null

    /** Mirrors legacy playDhikrSound. */
    fun play(id: Int, url: String, text: String, volume: Float = 1f) {
        if (isPlaying || isPreparing) return
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                prepareAndStart(id, url, text, volume)
            }
        }
    }

    /** Mirrors legacy toggleDhikrSound: the same id toggles the sound off. */
    fun toggle(id: Int, url: String, text: String, volume: Float = 1f) {
        if (currentlyPlayingId == id) stop() else play(id, url, text, volume)
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (!isPlaying) return
        viewModelScope.launch(Dispatchers.Main) {
            runCatching { player.pause() }
            isPlaying = false
        }
    }

    fun resume() {
        val player = mediaPlayer ?: return
        if (isPlaying) return
        viewModelScope.launch(Dispatchers.Main) {
            runCatching { player.start() }
            isPlaying = true
        }
    }

    fun stop() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                stopInternal(abandonFocus = true)
            }
        }
    }

    private fun prepareAndStart(id: Int, url: String, text: String, volume: Float) {
        stopInternal(abandonFocus = true)
        isPreparing = true
        try {
            val player = MediaPlayer()
            mediaPlayer = player
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            player.setVolume(volume, volume)
            player.setOnCompletionListener { stopInternal(abandonFocus = true) }
            player.setOnErrorListener { _, _, _ ->
                stopInternal(abandonFocus = true)
                true
            }
            player.setDataSource(url)
            player.prepare()

            audioFocusManager = AudioFocusManager(
                context = appContext,
                onPause = {
                    runCatching { mediaPlayer?.pause() }
                    isPlaying = false
                },
                onResume = {
                    runCatching { mediaPlayer?.start() }
                    isPlaying = true
                },
                onStop = { stopInternal(abandonFocus = false) },
            )
            if (audioFocusManager?.requestAudioFocus() != true) {
                stopInternal(abandonFocus = false)
                return
            }

            setupMediaSession(title = text, artist = "الذاكر")
            player.start()
            currentlyPlayingId = id
            currentlyPlayingText = text
            isPlaying = true
        } catch (error: Exception) {
            stopInternal(abandonFocus = true)
        } finally {
            isPreparing = false
        }
    }

    private fun setupMediaSession(title: String, artist: String) {
        val session = MediaSession(appContext, "alzaker_audio_player")
        mediaSession = session
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                .build(),
        )
        session.setPlaybackState(
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build(),
        )
        session.isActive = true
    }

    private fun stopInternal(abandonFocus: Boolean) {
        runCatching {
            mediaPlayer?.setOnCompletionListener(null)
            mediaPlayer?.setOnErrorListener(null)
            mediaPlayer?.release()
        }
        mediaPlayer = null
        if (abandonFocus) {
            audioFocusManager?.abandonAudioFocus()
        }
        audioFocusManager = null
        runCatching {
            mediaSession?.isActive = false
            mediaSession?.release()
        }
        mediaSession = null
        currentlyPlayingId = null
        currentlyPlayingText = null
        isPlaying = false
        isPreparing = false
    }

    override fun onCleared() {
        super.onCleared()
        stopInternal(abandonFocus = true)
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
