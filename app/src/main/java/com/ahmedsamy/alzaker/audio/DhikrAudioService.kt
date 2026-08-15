package com.ahmedsamy.alzaker.audio

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ahmedsamy.alzaker.MainActivity
import com.ahmedsamy.alzaker.R
import com.ahmedsamy.alzaker.reminder.AudioFocusManager
import com.ahmedsamy.alzaker.reminder.NotificationChannels
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground media service for dhikr audio playback, so the dhikr behaves like
 * a media player: a media-style notification (play/pause + stop actions) in
 * the shade, lock-screen / quick-settings controls via the active platform
 * [MediaSession], and playback that continues with the screen locked and after
 * the app is dismissed. Playback state is published through the companion
 * [StateFlow]s that [com.ahmedsamy.alzaker.ui.AudioPlayerViewModel] mirrors
 * into Compose state. The service stops itself on completion, error, or
 * permanent focus loss.
 */
class DhikrAudioService : Service() {

    companion object {
        private const val EXTRA_ID = "extra_dhikr_id"
        private const val EXTRA_URL = "extra_dhikr_url"
        private const val EXTRA_TEXT = "extra_dhikr_text"
        private const val EXTRA_VOLUME = "extra_dhikr_volume"

        const val ACTION_PLAY = "com.ahmedsamy.alzaker.action.PLAY_DHIKR"
        const val ACTION_PAUSE = "com.ahmedsamy.alzaker.action.PAUSE_DHIKR"
        const val ACTION_RESUME = "com.ahmedsamy.alzaker.action.RESUME_DHIKR"
        const val ACTION_STOP = "com.ahmedsamy.alzaker.action.STOP_DHIKR"

        private const val NOTIFICATION_ID = 2001

        private val _currentlyPlayingId = MutableStateFlow<Int?>(null)
        val currentlyPlayingId: StateFlow<Int?> = _currentlyPlayingId.asStateFlow()

        private val _currentlyPlayingText = MutableStateFlow<String?>(null)
        val currentlyPlayingText: StateFlow<String?> = _currentlyPlayingText.asStateFlow()

        private val _isPlaying = MutableStateFlow(false)
        val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

        private val _isPreparing = MutableStateFlow(false)
        val isPreparing: StateFlow<Boolean> = _isPreparing.asStateFlow()

        /** Starts playback of the given remote dhikr audio_url. */
        fun play(context: Context, id: Int, url: String, text: String, volume: Float) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, DhikrAudioService::class.java)
                    .setAction(ACTION_PLAY)
                    .putExtra(EXTRA_ID, id)
                    .putExtra(EXTRA_URL, url)
                    .putExtra(EXTRA_TEXT, text)
                    .putExtra(EXTRA_VOLUME, volume),
            )
        }

        fun pause(context: Context) = sendAction(context, ACTION_PAUSE)

        fun resume(context: Context) = sendAction(context, ACTION_RESUME)

        fun stop(context: Context) = sendAction(context, ACTION_STOP)

        private fun sendAction(context: Context, action: String) {
            context.startService(Intent(context, DhikrAudioService::class.java).setAction(action))
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null
    private var audioFocusManager: AudioFocusManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioFocusManager = AudioFocusManager(
            context = this,
            onPause = {
                runCatching { mediaPlayer?.pause() }
                _isPlaying.value = false
                refreshMediaState()
            },
            onResume = {
                runCatching { mediaPlayer?.start() }
                _isPlaying.value = true
                refreshMediaState()
            },
            onStop = ::stopPlayback,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val id = intent.getIntExtra(EXTRA_ID, -1)
                val url = intent.getStringExtra(EXTRA_URL)
                val text = intent.getStringExtra(EXTRA_TEXT)
                val volume = intent.getFloatExtra(EXTRA_VOLUME, 1f)
                if (id >= 0 && url != null && text != null) {
                    startPlayback(id, url, text, volume)
                }
            }
            ACTION_PAUSE -> pausePlayback()
            ACTION_RESUME -> resumePlayback()
            ACTION_STOP -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    private fun startPlayback(id: Int, url: String, text: String, volume: Float) {
        startForeground(NOTIFICATION_ID, buildMediaNotification())
        if (audioFocusManager?.requestAudioFocus() != true) {
            stopPlayback()
            return
        }
        releaseResources()
        _currentlyPlayingId.value = id
        _currentlyPlayingText.value = text
        _isPlaying.value = false
        _isPreparing.value = true
        refreshMediaState()
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
            player.setOnPreparedListener {
                setupMediaSession(text)
                runCatching { player.start() }
                _isPlaying.value = true
                _isPreparing.value = false
                refreshMediaState()
            }
            player.setOnCompletionListener { stopPlayback() }
            player.setOnErrorListener { _, _, _ ->
                stopPlayback()
                true
            }
            player.setDataSource(url)
            player.prepareAsync()
        } catch (error: Exception) {
            stopPlayback()
        }
    }

    private fun pausePlayback() {
        if (!_isPlaying.value) return
        runCatching { mediaPlayer?.pause() }
        _isPlaying.value = false
        refreshMediaState()
    }

    private fun resumePlayback() {
        val player = mediaPlayer ?: return
        if (_isPlaying.value) return
        runCatching { player.start() }
        _isPlaying.value = true
        refreshMediaState()
    }

    private fun stopPlayback() {
        releaseResources()
        _currentlyPlayingId.value = null
        _currentlyPlayingText.value = null
        _isPlaying.value = false
        _isPreparing.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setupMediaSession(title: String) {
        val session = MediaSession(this, "alzaker_dhikr_media")
        mediaSession = session
        session.setCallback(object : MediaSession.Callback() {
            override fun onPlay() = resumePlayback()
            override fun onPause() = pausePlayback()
            override fun onStop() = stopPlayback()
        })
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "الذاكر")
                .build(),
        )
        session.isActive = true
        refreshMediaState()
    }

    private fun refreshMediaState() {
        val state = when {
            _isPreparing.value -> PlaybackState.STATE_BUFFERING
            _isPlaying.value -> PlaybackState.STATE_PLAYING
            _currentlyPlayingId.value != null -> PlaybackState.STATE_PAUSED
            else -> PlaybackState.STATE_STOPPED
        }
        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_STOP or PlaybackState.ACTION_PLAY_PAUSE,
                )
                .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build(),
        )
        refreshNotification()
    }

    private fun refreshNotification() {
        runCatching {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildMediaNotification())
        }
    }

    private fun buildMediaNotification(): Notification {
        val isPlaying = _isPlaying.value
        val isPreparing = _isPreparing.value
        val playPausePending = PendingIntent.getService(
            this,
            NOTIFICATION_ID,
            Intent(this, DhikrAudioService::class.java).setAction(
                if (isPlaying) ACTION_PAUSE else ACTION_RESUME,
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopPending = PendingIntent.getService(
            this,
            NOTIFICATION_ID + 1,
            Intent(this, DhikrAudioService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val openAppPending = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID + 2,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, NotificationChannels.MEDIA_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (isPreparing) "جارٍ تحميل الذكر" else "الذاكر")
            .setContentText(_currentlyPlayingText.value ?: "ذكر قيد التشغيل")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setContentIntent(openAppPending)
            .addAction(
                if (isPlaying || isPreparing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "إيقاف مؤقت" else "استئناف",
                playPausePending,
            )
            .addAction(android.R.drawable.ic_lock_power_off, "إيقاف الذكر", stopPending)
            .build()
    }

    private fun releaseResources() {
        runCatching {
            mediaPlayer?.setOnCompletionListener(null)
            mediaPlayer?.setOnErrorListener(null)
            mediaPlayer?.setOnPreparedListener(null)
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
}
