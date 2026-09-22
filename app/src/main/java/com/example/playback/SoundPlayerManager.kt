package com.example.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.data.entity.SongEntity
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

class SoundOperatorPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = SoundPlayerManager.getInstance(this).getExoPlayer()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
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
}

class SoundPlayerManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

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
                } else {
                    stopTrackingProgress()
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

    fun playSong(song: SongEntity, playlist: List<SongEntity> = emptyList()) {
        _currentSong.value = song
        if (playlist.isNotEmpty()) {
            _currentPlaylist.value = playlist
        } else if (_currentPlaylist.value.none { it.id == song.id }) {
            _currentPlaylist.value = listOf(song)
        }

        try {
            val mediaItem = buildMediaItem(song)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            _durationMs.value = song.durationMs
            onSongChanged?.invoke(song)
        } catch (_: Exception) {
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
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (_currentSong.value != null) {
                exoPlayer.play()
            }
        }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun play() {
        if (_currentSong.value != null) {
            exoPlayer.play()
        }
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
        // If we are more than 3 seconds in, restart track
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

                // Animate DJ visualizer frequency bars
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
        // Rest state for visualizer
        _visualizerBands.value = List(8) { 0.1f }
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
