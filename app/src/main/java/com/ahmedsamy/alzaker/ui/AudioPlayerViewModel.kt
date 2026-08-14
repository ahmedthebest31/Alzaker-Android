package com.ahmedsamy.alzaker.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
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
 * In-app audio player for the audio drop buttons, mirroring the legacy
 * AudioContext: a single MediaPlayer with 'doNotMix' audio focus, a lock-screen
 * media session titled like the legacy 'الذاكر'/'تذكير' and the user's volume
 * applied. Playback stops on completion, focus loss, or [stop].
 */
class AudioPlayerViewModel(private val appContext: Context) : ViewModel() {

    var isPlaying by mutableStateOf(false)
        private set

    var isPreparing by mutableStateOf(false)
        private set

    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null
    private var audioFocusManager: AudioFocusManager? = null

    /**
     * Plays [resourceId] once. No-op while already playing or preparing.
     * [title] and [subtitle] feed the lock-screen media metadata; [volume]
     * mirrors the legacy audioVolume setting (0..1).
     */
    fun play(resourceId: Int, title: String, subtitle: String, volume: Float = 1f) {
        if (isPlaying || isPreparing) return
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                prepareAndStart(resourceId, title, subtitle, volume)
            }
        }
    }

    fun stop() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                stopInternal(abandonFocus = true)
            }
        }
    }

    private fun prepareAndStart(resourceId: Int, title: String, subtitle: String, volume: Float) {
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
            player.setDataSource(
                appContext,
                Uri.parse("android.resource://${appContext.packageName}/$resourceId"),
            )
            player.prepare()

            audioFocusManager = AudioFocusManager(
                context = appContext,
                onPause = { mediaPlayer?.let { runCatching { it.pause() } } },
                onResume = { mediaPlayer?.let { runCatching { it.start() } } },
                onStop = { stopInternal(abandonFocus = false) },
            )
            if (audioFocusManager?.requestAudioFocus() != true) {
                stopInternal(abandonFocus = false)
                return
            }

            setupMediaSession(title, subtitle)
            player.start()
            isPlaying = true
        } catch (error: Exception) {
            stopInternal(abandonFocus = true)
        } finally {
            isPreparing = false
        }
    }

    private fun setupMediaSession(title: String, subtitle: String) {
        val session = MediaSession(appContext, "alzaker_audio_player")
        mediaSession = session
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, subtitle)
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
