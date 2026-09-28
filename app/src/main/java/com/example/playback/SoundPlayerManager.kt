package com.example.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes as AndroidAudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.R
import com.example.data.entity.SongEntity
import com.example.data.youtube.YouTubeVideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin

/**
 * Foreground MediaPlaybackService providing persistent background audio playback,
 * system lockscreen controls, and Android notification controls for both local and YouTube audio.
 */
class SoundOperatorPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val player = SoundPlayerManager.getInstance(this).getExoPlayer()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val manager = SoundPlayerManager.getInstance(this)

        when (action) {
            ACTION_PLAY_PAUSE -> manager.togglePlayPause()
            ACTION_NEXT -> manager.playNext()
            ACTION_PREVIOUS -> manager.playPrevious()
            ACTION_STOP -> {
                manager.stopAllPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val notification = manager.buildNotification(this, mediaSession)
        if (notification != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } catch (_: Exception) {}
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sound Operator Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Playback controls and status for DJ music & YouTube audio"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "sound_operator_playback_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.action.START"
        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_UPDATE = "com.example.action.UPDATE"
    }
}

/**
 * Unified playback manager responsible for audio focus, wake lock, foreground service coordination,
 * and seamless audio playback across local tracks and YouTube video streams.
 */
class SoundPlayerManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        setAudioAttributes(audioAttributes, true)
        setHandleAudioBecomingNoisy(true)
    }

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _currentYouTubeVideo = MutableStateFlow<YouTubeVideoItem?>(null)
    val currentYouTubeVideo: StateFlow<YouTubeVideoItem?> = _currentYouTubeVideo.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isMasterMuted = MutableStateFlow(false)
    val isMasterMuted: StateFlow<Boolean> = _isMasterMuted.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<SongEntity>>(emptyList())
    val currentPlaylist: StateFlow<List<SongEntity>> = _currentPlaylist.asStateFlow()

    private var currentArtworkBitmap: Bitmap? = null

    // Real-time audio visualizer bands (8 frequency bars for DJ deck)
    private val _visualizerBands = MutableStateFlow(List(8) { 0.1f })
    val visualizerBands: StateFlow<List<Float>> = _visualizerBands.asStateFlow()

    var onSongEnded: (() -> Unit)? = null
    var onSongChanged: ((SongEntity) -> Unit)? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startTrackingProgress()
                    acquireWakeLock()
                    updateForegroundNotification()
                } else {
                    stopTrackingProgress()
                    releaseWakeLock()
                    updateForegroundNotification()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    handleTrackEnded()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _currentPositionMs.value = exoPlayer.currentPosition
            }
        })
    }

    fun getExoPlayer(): ExoPlayer = exoPlayer

    // --- Audio Focus Handling ---

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AndroidAudioAttributes.Builder()
                .setUsage(AndroidAudioAttributes.USAGE_MEDIA)
                .setContentType(AndroidAudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            pause()
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            play()
                        }
                    }
                }
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                        AudioManager.AUDIOFOCUS_GAIN -> play()
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    // --- WakeLock Management ---

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "SoundOperator:PlaybackWakeLock"
                )
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(60 * 60 * 1000L) // 60 min safeguard
            }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    // --- Playback Management ---

    fun playSong(song: SongEntity, playlist: List<SongEntity> = emptyList()) {
        // Stop YouTube playback if active so streams do not overlap
        if (_currentYouTubeVideo.value != null) {
            YouTubePlayerBridge.stop()
            _currentYouTubeVideo.value = null
        }

        _currentSong.value = song
        if (playlist.isNotEmpty()) {
            _currentPlaylist.value = playlist
        } else if (_currentPlaylist.value.none { it.id == song.id }) {
            _currentPlaylist.value = listOf(song)
        }

        try {
            requestAudioFocus()
            loadArtwork(song.coverColorHex, true)
            val mediaItem = buildMediaItem(song)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            _durationMs.value = song.durationMs
            onSongChanged?.invoke(song)
            acquireWakeLock()
            updateForegroundNotification()
        } catch (_: Exception) {}
    }

    fun onYouTubeStarted(video: YouTubeVideoItem) {
        // Pause local player
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
        _currentSong.value = null
        _currentYouTubeVideo.value = video
        requestAudioFocus()
        loadArtwork(video.thumbnailUrl, false)
        acquireWakeLock()
        updateForegroundNotification()
    }

    private fun loadArtwork(urlOrColor: String?, isLocal: Boolean) {
        scope.launch(Dispatchers.IO) {
            try {
                if (!urlOrColor.isNullOrBlank() && (urlOrColor.startsWith("http") || urlOrColor.startsWith("content:"))) {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(urlOrColor)
                        .allowHardware(false)
                        .size(256, 256)
                        .build()
                    val result = (loader.execute(request) as? SuccessResult)?.drawable
                    currentArtworkBitmap = (result as? BitmapDrawable)?.bitmap
                } else {
                    val colorInt = try {
                        if (!urlOrColor.isNullOrBlank()) Color.parseColor(urlOrColor)
                        else Color.parseColor("#FFB300")
                    } catch (_: Exception) {
                        Color.parseColor("#FFB300")
                    }
                    val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorInt
                    }
                    canvas.drawColor(Color.DKGRAY)
                    canvas.drawCircle(64f, 64f, 48f, paint)
                    paint.color = Color.BLACK
                    canvas.drawCircle(64f, 64f, 16f, paint)
                    currentArtworkBitmap = bitmap
                }
                updateForegroundNotification()
            } catch (_: Exception) {}
        }
    }

    fun onYouTubeStateChanged(isPlaying: Boolean) {
        if (isPlaying) {
            acquireWakeLock()
        } else {
            releaseWakeLock()
        }
        updateForegroundNotification()
    }

    fun stopYouTubePlayback() {
        YouTubePlayerBridge.stop()
        _currentYouTubeVideo.value = null
        releaseWakeLock()
        if (_currentSong.value == null) {
            abandonAudioFocus()
            stopForegroundService()
        } else {
            updateForegroundNotification()
        }
    }

    private fun buildMediaItem(song: SongEntity): MediaItem {
        val uri = if (song.filePath.startsWith("content://") || song.filePath.startsWith("http")) {
            Uri.parse(song.filePath)
        } else {
            Uri.fromFile(File(song.filePath))
        }

        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .build()
            )
            .build()
    }

    fun togglePlayPause() {
        if (_currentYouTubeVideo.value != null) {
            YouTubePlayerBridge.togglePlayPause()
            updateForegroundNotification()
        } else if (_currentSong.value != null) {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                requestAudioFocus()
                exoPlayer.play()
            }
            updateForegroundNotification()
        }
    }

    fun pause() {
        if (_currentYouTubeVideo.value != null) {
            YouTubePlayerBridge.pause()
        } else {
            exoPlayer.pause()
        }
        releaseWakeLock()
        updateForegroundNotification()
    }

    fun play() {
        requestAudioFocus()
        acquireWakeLock()
        if (_currentYouTubeVideo.value != null) {
            YouTubePlayerBridge.play()
        } else if (_currentSong.value != null) {
            exoPlayer.play()
        }
        updateForegroundNotification()
    }

    fun stopAllPlayback() {
        exoPlayer.stop()
        _currentSong.value = null
        _isPlaying.value = false
        YouTubePlayerBridge.stop()
        _currentYouTubeVideo.value = null
        releaseWakeLock()
        abandonAudioFocus()
        stopForegroundService()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1L)))
        _currentPositionMs.value = exoPlayer.currentPosition
    }

    fun seekRelative(offsetMs: Long) {
        val target = (exoPlayer.currentPosition + offsetMs).coerceIn(0L, _durationMs.value.coerceAtLeast(1L))
        exoPlayer.seekTo(target)
        _currentPositionMs.value = exoPlayer.currentPosition
    }

    fun jumpToCue(percentage: Float) {
        val target = ((_durationMs.value) * percentage.coerceIn(0f, 1f)).toLong()
        seekTo(target)
    }

    fun playNext() {
        if (_currentYouTubeVideo.value != null) {
            YouTubePlayerBridge.onNextTrack?.invoke()
            return
        }

        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex != -1) {
            val nextIndex = if (_isShuffleEnabled.value) {
                playlist.indices.random()
            } else {
                (currentIndex + 1) % playlist.size
            }
            playSong(playlist[nextIndex], playlist)
        } else {
            playSong(playlist.first(), playlist)
        }
    }

    fun playPrevious() {
        if (_currentYouTubeVideo.value != null) {
            YouTubePlayerBridge.onPreviousTrack?.invoke()
            return
        }

        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0)
            return
        }

        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex != -1) {
            val prevIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
            playSong(playlist[prevIndex], playlist)
        } else {
            playSong(playlist.last(), playlist)
        }
    }

    private fun handleTrackEnded() {
        when (_repeatMode.value) {
            Player.REPEAT_MODE_ONE -> {
                seekTo(0)
                exoPlayer.play()
            }
            Player.REPEAT_MODE_ALL -> {
                playNext()
            }
            else -> {
                val playlist = _currentPlaylist.value
                val currentIndex = playlist.indexOfFirst { it.id == _currentSong.value?.id }
                if (currentIndex != -1 && currentIndex < playlist.size - 1) {
                    playNext()
                } else {
                    _isPlaying.value = false
                    onSongEnded?.invoke()
                }
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.8f, 1.2f)
        _playbackSpeed.value = clamped
        exoPlayer.playbackParameters = PlaybackParameters(clamped)
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        exoPlayer.repeatMode = nextMode
    }

    fun toggleShuffle() {
        val newState = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newState
        exoPlayer.shuffleModeEnabled = newState
    }

    fun setMasterMute(muted: Boolean) {
        _isMasterMuted.value = muted
        exoPlayer.volume = if (muted) 0f else 1f
    }

    fun toggleMasterMute() {
        setMasterMute(!_isMasterMuted.value)
    }

    private fun startTrackingProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var step = 0
            while (isActive) {
                _currentPositionMs.value = exoPlayer.currentPosition
                if (exoPlayer.duration > 0) {
                    _durationMs.value = exoPlayer.duration
                }

                step++
                val songBpm = _currentSong.value?.bpm ?: 120
                val tempoFactor = songBpm / 120f
                _visualizerBands.value = List(8) { bandIdx ->
                    val wave = sin((step * 0.3 * tempoFactor) + bandIdx * 0.8)
                    val base = 0.25f + 0.2f * ((bandIdx % 3) + 1)
                    val dynamic = (wave * 0.35f).toFloat()
                    (base + dynamic).coerceIn(0.08f, 0.98f)
                }

                delay(100)
            }
        }
    }

    private fun stopTrackingProgress() {
        progressJob?.cancel()
        progressJob = null
        _visualizerBands.value = List(8) { 0.1f }
    }

    // --- Foreground Service Notification ---

    fun updateForegroundNotification() {
        val intent = Intent(context, SoundOperatorPlaybackService::class.java).apply {
            action = SoundOperatorPlaybackService.ACTION_UPDATE
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {}
    }

    fun stopForegroundService() {
        val intent = Intent(context, SoundOperatorPlaybackService::class.java).apply {
            action = SoundOperatorPlaybackService.ACTION_STOP
        }
        try {
            context.startService(intent)
        } catch (_: Exception) {}
    }

    fun buildNotification(serviceContext: Context, session: MediaSession? = null): Notification? {
        val ytVideo = _currentYouTubeVideo.value
        val localSong = _currentSong.value

        if (ytVideo == null && localSong == null) {
            return null
        }

        val isPlaybackActive = if (ytVideo != null) {
            YouTubePlayerBridge.isPlaying.value
        } else {
            _isPlaying.value
        }

        val title = ytVideo?.title ?: localSong?.title ?: "Sound Operator"
        val subtitle = if (ytVideo != null) {
            "${ytVideo.channelTitle} • YouTube Playback"
        } else {
            "${localSong?.artist} • ${localSong?.album}"
        }

        val contentIntent = Intent(serviceContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            serviceContext,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            serviceContext, 1,
            Intent(serviceContext, SoundOperatorPlaybackService::class.java).apply {
                action = SoundOperatorPlaybackService.ACTION_PREVIOUS
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPausePendingIntent = PendingIntent.getService(
            serviceContext, 2,
            Intent(serviceContext, SoundOperatorPlaybackService::class.java).apply {
                action = SoundOperatorPlaybackService.ACTION_PLAY_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            serviceContext, 3,
            Intent(serviceContext, SoundOperatorPlaybackService::class.java).apply {
                action = SoundOperatorPlaybackService.ACTION_NEXT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            serviceContext, 4,
            Intent(serviceContext, SoundOperatorPlaybackService::class.java).apply {
                action = SoundOperatorPlaybackService.ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaybackActive) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseLabel = if (isPlaybackActive) "Pause" else "Play"

        val builder = NotificationCompat.Builder(serviceContext, SoundOperatorPlaybackService.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSubText("Sound Operator")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaybackActive)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseLabel, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)

        currentArtworkBitmap?.let { bitmap ->
            builder.setLargeIcon(bitmap)
        }

        try {
            if (session != null) {
                val mediaStyle = MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1, 2)
                builder.setStyle(mediaStyle)
            }
        } catch (_: Exception) {}

        return builder.build()
    }

    companion object {
        @Volatile
        private var INSTANCE: SoundPlayerManager? = null

        fun getInstance(context: Context): SoundPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundPlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
