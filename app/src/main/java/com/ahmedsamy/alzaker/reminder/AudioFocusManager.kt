package com.ahmedsamy.alzaker.reminder

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

/**
 * Manages audio focus for the audio drop, mirroring the legacy expo-audio
 * interruptionMode 'doNotMix' (AUDIOFOCUS_GAIN). Requires API 26+, which
 * matches the app minSdk, so no legacy requestAudioFocus path is needed.
 *
 * @param onPause called on transient focus loss (e.g. a phone call).
 * @param onResume called when focus is regained.
 * @param onStop called on permanent focus loss (playback must end).
 */
class AudioFocusManager(
    context: Context,
    private val onPause: () -> Unit,
    private val onResume: () -> Unit,
    private val onStop: () -> Unit,
) {
    private val audioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> onResume()
            AudioManager.AUDIOFOCUS_LOSS -> onStop()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onPause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onPause()
            else -> Unit
        }
    }

    private val focusRequest: AudioFocusRequest = AudioFocusRequest.Builder(
        AudioManager.AUDIOFOCUS_GAIN,
    )
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        .setOnAudioFocusChangeListener(listener)
        .build()

    /** Requests AUDIOFOCUS_GAIN; returns true when granted. */
    fun requestAudioFocus(): Boolean =
        audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED

    fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(focusRequest)
    }
}
