package com.ahmedsamy.alzaker.reminder

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ahmedsamy.alzaker.R
import kotlin.random.Random

/**
 * Foreground service that plays one random audio drop from
 * res/raw/audio_drop_1..14.mp3, mirroring the legacy AudioDropWorker (random
 * pick, doNotMix audio focus, lock-screen media session titled 'الذاكر' /
 * 'تذكير'). Runs only for the duration of the clip: it starts playback,
 * reports a media notification, and stops itself on completion or focus loss.
 */
class AudioReminderService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null
    private var audioFocusManager: AudioFocusManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioFocusManager = AudioFocusManager(
            context = this,
            onPause = { mediaPlayer?.let { runCatching { it.pause() } } },
            onResume = { mediaPlayer?.let { runCatching { it.start() } } },
            onStop = ::stopPlayback,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // On Android 12+ a foreground service may NOT be started from an
        // INEXACT alarm broadcast (only exact alarms are exempt), and on
        // Android 13+ the notification may be suppressed. Degrade gracefully:
        // still attempt playback best-effort instead of failing silently.
        val foregroundStarted = runCatching {
            startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())
            true
        }.getOrDefault(false)
        if (!foregroundStarted) {
            Log.w(TAG, "startForeground failed; playing without foreground notification")
        }
        if (audioFocusManager?.requestAudioFocus() != true) {
            stopPlayback()
            return START_NOT_STICKY
        }
        startPlayback()
        return START_NOT_STICKY
    }

    private fun buildForegroundNotification(): Notification =
        NotificationCompat.Builder(this, NotificationChannels.TADHKIR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("الذاكر")
            .setContentText("تذكير")
            .setOngoing(true)
            .build()

    private fun startPlayback() {
        val player = MediaPlayer()
        mediaPlayer = player
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        player.setOnCompletionListener { stopPlayback() }
        player.setOnErrorListener { _, _, _ ->
            stopPlayback()
            true
        }

        val audioResId = AUDIO_DROP_RES_IDS[Random.nextInt(AUDIO_DROP_RES_IDS.size)]
        try {
            player.setDataSource(this, Uri.parse("android.resource://$packageName/$audioResId"))
            player.prepare()
        } catch (error: Exception) {
            stopPlayback()
            return
        }

        setupMediaSession()
        player.start()
    }

    private fun setupMediaSession() {
        val session = MediaSession(this, "alzaker_audio_reminder")
        mediaSession = session
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "الذاكر")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "تذكير")
                .build(),
        )
        session.setPlaybackState(
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build(),
        )
        session.isActive = true
    }

    private fun stopPlayback() {
        releaseResources()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseResources() {
        runCatching {
            mediaPlayer?.setOnCompletionListener(null)
            mediaPlayer?.release()
        }
        mediaPlayer = null
        audioFocusManager?.abandonAudioFocus()
        runCatching {
            mediaSession?.isActive = false
            mediaSession?.release()
        }
        mediaSession = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    private companion object {
        const val TAG = "AudioReminderService"
        const val FOREGROUND_NOTIFICATION_ID = 1002
        val AUDIO_DROP_RES_IDS = intArrayOf(
            R.raw.audio_drop_1,
            R.raw.audio_drop_2,
            R.raw.audio_drop_3,
            R.raw.audio_drop_4,
            R.raw.audio_drop_5,
            R.raw.audio_drop_6,
            R.raw.audio_drop_7,
            R.raw.audio_drop_8,
            R.raw.audio_drop_9,
            R.raw.audio_drop_10,
            R.raw.audio_drop_11,
            R.raw.audio_drop_12,
            R.raw.audio_drop_13,
            R.raw.audio_drop_14,
        )
    }
}
