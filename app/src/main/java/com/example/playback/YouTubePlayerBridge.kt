package com.example.playback

import android.webkit.WebView
import com.example.data.youtube.YouTubeVideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Singleton bridge for controlling the active official YouTube WebView player instance
 * from background services, media notifications, audio focus callbacks, and the mini player.
 */
object YouTubePlayerBridge {
    private var webViewRef: WeakReference<WebView>? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentVideo = MutableStateFlow<YouTubeVideoItem?>(null)
    val currentVideo: StateFlow<YouTubeVideoItem?> = _currentVideo.asStateFlow()

    var onNextTrack: (() -> Unit)? = null
    var onPreviousTrack: (() -> Unit)? = null
    var onStopRequested: (() -> Unit)? = null

    fun registerWebView(webView: WebView, video: YouTubeVideoItem) {
        webViewRef = WeakReference(webView)
        _currentVideo.value = video
    }

    fun unregisterWebView(webView: WebView?) {
        if (webViewRef?.get() == webView) {
            webViewRef = null
        }
    }

    fun onReady() {
        // Player is ready
    }

    fun onError(errorCode: Int) {
        // Keep error state tracked
        if (errorCode != 0) {
            _isPlaying.value = false
        }
    }

    fun onStateChange(state: Int) {
        // YouTube IFrame API state codes:
        // -1 = unstarted, 0 = ended, 1 = playing, 2 = paused, 3 = buffering, 5 = cued
        when (state) {
            1 -> _isPlaying.value = true
            2 -> _isPlaying.value = false
            0 -> {
                _isPlaying.value = false
                // Auto play next video if available
                onNextTrack?.invoke()
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        webViewRef?.get()?.let { webView ->
            webView.post {
                webView.evaluateJavascript(
                    "try { if (player && player.playVideo) player.playVideo(); } catch(e){}",
                    null
                )
            }
        }
        _isPlaying.value = true
    }

    fun pause() {
        webViewRef?.get()?.let { webView ->
            webView.post {
                webView.evaluateJavascript(
                    "try { if (player && player.pauseVideo) player.pauseVideo(); } catch(e){}",
                    null
                )
            }
        }
        _isPlaying.value = false
    }

    fun stop() {
        webViewRef?.get()?.let { webView ->
            webView.post {
                webView.evaluateJavascript(
                    "try { if (player && player.stopVideo) player.stopVideo(); } catch(e){}",
                    null
                )
            }
        }
        _isPlaying.value = false
        _currentVideo.value = null
        webViewRef = null
    }

    fun clear() {
        webViewRef = null
        _isPlaying.value = false
        _currentVideo.value = null
    }
}
