package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.youtube.YouTubeApiClient
import com.example.data.youtube.YouTubeVideoItem
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjObsidianBlack
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjTextTertiary

/**
 * Playback integration mode for YouTube IFrame player on Android WebView:
 * - OPTION_A_BASE_URL: Uses WebView.loadDataWithBaseURL("https://<APPLICATION_ID>/", ...) with official IFrame HTML
 * - OPTION_B_LOAD_URL: Uses WebView.loadUrl("https://www.youtube.com/embed/...", extraHeaders = { Referer: "https://<APPLICATION_ID>" })
 */
enum class PlaybackMode(val label: String) {
    OPTION_A_BASE_URL("Option A: BaseURL Document"),
    OPTION_B_LOAD_URL("Option B: Direct Embed URL")
}

/**
 * JavaScript interface bridge for receiving raw YouTube IFrame API events.
 */
class YouTubeJsInterface(
    private val onReadyCallback: () -> Unit,
    private val onErrorCallback: (Int) -> Unit,
    private val onStateChangeCallback: (Int) -> Unit
) {
    @JavascriptInterface
    fun onReady() {
        Log.i("YouTubePlayer", "YouTube IFrame API: onReady callback received")
        onReadyCallback()
    }

    @JavascriptInterface
    fun onError(errorCode: Int) {
        Log.w("YouTubePlayer", "YouTube IFrame API: onError callback received raw errorCode=$errorCode")
        onErrorCallback(errorCode)
    }

    @JavascriptInterface
    fun onStateChange(state: Int) {
        Log.d("YouTubePlayer", "YouTube IFrame API: onStateChange callback received state=$state")
        onStateChangeCallback(state)
    }
}

/**
 * Helper to retrieve actual Android System WebView package version.
 */
fun getDeviceWebViewVersion(context: Context): String {
    return try {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo("com.google.android.webview", android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo("com.google.android.webview", 0)
        }
        "Android System WebView ${info.versionName}"
    } catch (_: Exception) {
        try {
            val ua = WebSettings.getDefaultUserAgent(context)
            val chromeMatch = ua.split(" ").find { it.startsWith("Chrome/") }
            chromeMatch ?: "Android WebView (SDK ${Build.VERSION.SDK_INT})"
        } catch (_: Exception) {
            "Android SDK ${Build.VERSION.SDK_INT}"
        }
    }
}

/**
 * Convert YouTube player state integer into human-readable name.
 */
fun describePlayerState(state: Int): String = when (state) {
    -1 -> "UNSTARTED (-1)"
    0 -> "ENDED (0)"
    1 -> "PLAYING (1)"
    2 -> "PAUSED (2)"
    3 -> "BUFFERING (3)"
    5 -> "CUED (5)"
    else -> "STATE ($state)"
}

/**
 * Official YouTube Embedded Player with real-time diagnostic panel.
 * Strictly uses the official YouTube IFrame player, avoids fake error mappings,
 * captures raw error codes, and supports instant toggling between Option A and Option B.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeEmbeddedPlayer(
    video: YouTubeVideoItem,
    onClose: () -> Unit,
    onSwitchTrack: ((YouTubeVideoItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val applicationId = context.packageName // Real applicationId: com.aistudio.soundoperator.djops
    val baseUrl = "https://$applicationId/"
    val refererHeader = "https://$applicationId"
    val webViewVersion = remember { getDeviceWebViewVersion(context) }

    var playbackMode by remember { mutableStateOf(PlaybackMode.OPTION_A_BASE_URL) }
    var isPlayerLoading by remember { mutableStateOf(true) }
    var rawErrorCode by remember { mutableIntStateOf(0) }
    var playerState by remember { mutableIntStateOf(-1) }
    var isDebugExpanded by remember { mutableStateOf(true) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    var lastWebViewError by remember { mutableStateOf<String?>(null) }
    var lastHttpError by remember { mutableStateOf<String?>(null) }
    val consoleLogs = remember { mutableStateListOf<String>() }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val actualEmbedUrl = "https://www.youtube.com/embed/${video.videoId}?enablejsapi=1&autoplay=1&playsinline=1&controls=1&rel=0&fs=1&origin=$refererHeader"

    // Intercept hardware back button to cleanly close player
    BackHandler {
        onClose()
    }

    // Stop playback and destroy WebView when Composable is disposed
    DisposableEffect(video.videoId, reloadTrigger, playbackMode) {
        onDispose {
            try {
                webViewInstance?.evaluateJavascript(
                    "try { if (player && player.stopVideo) player.stopVideo(); } catch(e){}",
                    null
                )
                webViewInstance?.onPause()
                webViewInstance?.destroy()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DjElevatedCard)
            .border(1.dp, DjCrimsonCue.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
        // Player Header: Back button, Status indicator, Title & Close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DjDeepSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DjElevatedCard)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Results",
                        tint = DjTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (playerState == 1) DjNeonEmerald
                            else if (playerState == 2) DjAmberGold
                            else if (rawErrorCode != 0) DjCrimsonCue
                            else DjTextTertiary
                        )
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = DjTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${video.channelTitle} • ${video.durationText} • ${playbackMode.label}",
                        color = DjTextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Player",
                    tint = DjTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 16:9 Official YouTube Video Canvas (WebView always remains attached so user can inspect it)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            key("${video.videoId}_${playbackMode.name}_$reloadTrigger") {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        createConfiguredYouTubeWebView(
                        context = ctx,
                        videoId = video.videoId,
                        mode = playbackMode,
                        applicationId = applicationId,
                        baseUrl = baseUrl,
                        refererHeader = refererHeader,
                        onReady = {
                            isPlayerLoading = false
                        },
                        onError = { err ->
                            isPlayerLoading = false
                            rawErrorCode = err
                        },
                        onStateChange = { state ->
                            playerState = state
                            if (state == 1) {
                                isPlayerLoading = false
                            }
                        },
                        onConsoleMsg = { msg ->
                            if (consoleLogs.size >= 8) consoleLogs.removeAt(0)
                            consoleLogs.add(msg)
                        },
                        onWebViewErrorDesc = { desc ->
                            lastWebViewError = desc
                        },
                        onHttpErrorDesc = { desc ->
                            lastHttpError = desc
                        }
                    ).also { webViewInstance = it }
                }
            )
            }

            // Non-blocking loading indicator before player emits onReady
            if (isPlayerLoading && rawErrorCode == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = DjCrimsonCue,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading Official YouTube Player (${playbackMode.label})...",
                            color = DjTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Real Error Banner: Displayed when YouTube IFrame API returns a non-zero error code
        if (rawErrorCode != 0) {
            val (errorHeadline, errorDetail) = when (rawErrorCode) {
                2 -> Pair(
                    "Invalid Parameter (YouTube API Error 2)",
                    "The embed request contains an invalid parameter value. Verify origin or parameters."
                )
                5 -> Pair(
                    "HTML5 Engine Error (YouTube API Error 5)",
                    "The requested video cannot be played in the HTML5 player engine on this device."
                )
                100 -> Pair(
                    "Video Unavailable (YouTube API Error 100)",
                    "This video was removed or marked private by the publisher on YouTube."
                )
                101 -> Pair(
                    "Embedding Disabled (YouTube API Error 101)",
                    "The content owner has disabled playback in third-party embedded players."
                )
                150 -> Pair(
                    "Embedding Disabled (YouTube API Error 150)",
                    "The content owner does not allow embedded playback for this video on application origin ($applicationId)."
                )
                153 -> Pair(
                    "Player Configuration (YouTube API Error 153)",
                    "YouTube reported configuration error 153. Try switching to Option B below."
                )
                else -> Pair(
                    "YouTube IFrame API Error $rawErrorCode",
                    "Raw integer error code returned directly by YouTube IFrame Player: $rawErrorCode"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DjCrimsonCue.copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = DjCrimsonCue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = errorHeadline,
                        color = DjCrimsonCue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = errorDetail,
                    color = DjTextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=${video.videoId}")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DjCrimsonCue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open in YouTube", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            rawErrorCode = 0
                            isPlayerLoading = true
                            playbackMode = if (playbackMode == PlaybackMode.OPTION_A_BASE_URL) {
                                PlaybackMode.OPTION_B_LOAD_URL
                            } else {
                                PlaybackMode.OPTION_A_BASE_URL
                            }
                            reloadTrigger++
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (playbackMode == PlaybackMode.OPTION_A_BASE_URL) "Try Option B" else "Try Option A",
                            color = DjAmberGold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Diagnostic & Playback Debug Panel (Expandable for on-device real-time inspection)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DjDeepSurface)
                .border(1.dp, DjBorderOutline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDebugExpanded = !isDebugExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = if (rawErrorCode != 0) DjCrimsonCue else DjAmberGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "REAL PLAYBACK DIAGNOSTICS",
                        color = DjTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (rawErrorCode != 0) DjCrimsonCue.copy(alpha = 0.2f)
                                else if (playerState == 1) DjNeonEmerald.copy(alpha = 0.2f)
                                else DjAmberGold.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (rawErrorCode != 0) "ERROR $rawErrorCode" else describePlayerState(playerState),
                            color = if (rawErrorCode != 0) DjCrimsonCue else if (playerState == 1) DjNeonEmerald else DjAmberGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Icon(
                    imageVector = if (isDebugExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = DjTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isDebugExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DebugInfoRow("Real YouTube Error", if (rawErrorCode == 0) "0 (No Error Reported)" else "$rawErrorCode")
                    DebugInfoRow("Player State", describePlayerState(playerState))
                    DebugInfoRow("Playback Mode", playbackMode.label)
                    DebugInfoRow("Application ID", applicationId)
                    DebugInfoRow("Base URL (Option A)", baseUrl)
                    DebugInfoRow("HTTP Referer", refererHeader)
                    DebugInfoRow("Embed URL", actualEmbedUrl)
                    DebugInfoRow("Device WebView", webViewVersion)
                    DebugInfoRow("WebView Error", lastWebViewError ?: "None")
                    DebugInfoRow("HTTP Error", lastHttpError ?: "None")

                    if (consoleLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recent WebView Console Messages:",
                            color = DjTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        consoleLogs.takeLast(4).forEach { logLine ->
                            Text(
                                text = logLine,
                                color = DjTextTertiary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Diagnostic Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                rawErrorCode = 0
                                isPlayerLoading = true
                                playbackMode = if (playbackMode == PlaybackMode.OPTION_A_BASE_URL) {
                                    PlaybackMode.OPTION_B_LOAD_URL
                                } else {
                                    PlaybackMode.OPTION_A_BASE_URL
                                }
                                reloadTrigger++
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (playbackMode == PlaybackMode.OPTION_A_BASE_URL) "Switch to Option B" else "Switch to Option A",
                                fontSize = 10.sp,
                                color = DjAmberGold
                            )
                        }

                        Button(
                            onClick = {
                                if (video.videoId != "M7lc1UVf-VE") {
                                    val officialSample = YouTubeApiClient.sampleEventVideos.firstOrNull { it.videoId == "M7lc1UVf-VE" }
                                        ?: YouTubeVideoItem(
                                            videoId = "M7lc1UVf-VE",
                                            title = "YouTube Developers Official Demo: Submitting Your App",
                                            channelTitle = "Google Developers",
                                            thumbnailUrl = "https://img.youtube.com/vi/M7lc1UVf-VE/hqdefault.jpg",
                                            durationText = "03:49"
                                        )
                                    onSwitchTrack?.invoke(officialSample)
                                } else {
                                    rawErrorCode = 0
                                    isPlayerLoading = true
                                    reloadTrigger++
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DjCrimsonCue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.SmartDisplay, null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test M7lc1UVf-VE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                rawErrorCode = 0
                                isPlayerLoading = true
                                reloadTrigger++
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DjElevatedCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = DjTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun DebugInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = DjTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = DjTextPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Creates and configures the official Android WebView for YouTube IFrame playback:
 * - Option A: WebView.loadDataWithBaseURL("https://<APPLICATION_ID>/", html, "text/html", "UTF-8", null)
 * - Option B: WebView.loadUrl("https://www.youtube.com/embed/VIDEO_ID?...", headers = { "Referer": "https://<APPLICATION_ID>" })
 */
@SuppressLint("SetJavaScriptEnabled")
private fun createConfiguredYouTubeWebView(
    context: Context,
    videoId: String,
    mode: PlaybackMode,
    applicationId: String,
    baseUrl: String,
    refererHeader: String,
    onReady: () -> Unit,
    onError: (Int) -> Unit,
    onStateChange: (Int) -> Unit,
    onConsoleMsg: (String) -> Unit,
    onWebViewErrorDesc: (String) -> Unit,
    onHttpErrorDesc: (String) -> Unit
): WebView {
    Log.i("YouTubePlayer", "Initializing YouTube WebView with mode=$mode, appId=$applicationId, videoId=$videoId")

    // Ensure WebView Code Cache directories exist so Chromium doesn't fail with simple_file_enumerator
    try {
        val paths = listOf(
            "WebView",
            "WebView/Default",
            "WebView/Default/HTTP Cache",
            "WebView/Default/HTTP Cache/Code Cache",
            "WebView/Default/HTTP Cache/Code Cache/js",
            "WebView/Default/HTTP Cache/Code Cache/wasm"
        )
        paths.forEach { path ->
            val dir = java.io.File(context.cacheDir, path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    } catch (_: Exception) {}

    // Ensure cookies are enabled for YouTube session integrity tokens
    val cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)

    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(android.graphics.Color.BLACK)

        // Mandatory for cross-origin YouTube iframe session tokens
        cookieManager.setAcceptThirdPartyCookies(this, true)

        settings.apply {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            databaseEnabled = true
            allowContentAccess = true
            allowFileAccess = false
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                val msg = consoleMessage?.message() ?: ""
                val level = consoleMessage?.messageLevel()?.name ?: "LOG"
                val line = consoleMessage?.lineNumber() ?: 0
                val entry = "[$level] $msg (line $line)"
                Log.d("YouTubePlayer", "WebView Console: $entry")
                onConsoleMsg(entry)
                return true
            }
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                val host = uri.host?.lowercase() ?: ""

                // Sub-frame internal resource loads must never be intercepted
                if (request.isForMainFrame == false) {
                    return false
                }

                // YouTube internal domains & app origin stay inside the player
                val isInternal = host.endsWith("youtube.com") ||
                        host.endsWith("youtube-nocookie.com") ||
                        host.endsWith("googlevideo.com") ||
                        host.endsWith("ytimg.com") ||
                        host.endsWith("doubleclick.net") ||
                        host.endsWith("google.com") ||
                        host == applicationId ||
                        host.endsWith(applicationId)

                if (!isInternal) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                        return true
                    } catch (_: Exception) {}
                }
                return false
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                val uri = Uri.parse(url)
                val host = uri.host?.lowercase() ?: ""
                val isInternal = host.endsWith("youtube.com") ||
                        host.endsWith("youtube-nocookie.com") ||
                        host.endsWith("googlevideo.com") ||
                        host.endsWith("ytimg.com") ||
                        host.endsWith("doubleclick.net") ||
                        host.endsWith("google.com") ||
                        host == applicationId ||
                        host.endsWith(applicationId)

                if (!isInternal) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                        return true
                    } catch (_: Exception) {}
                }
                return false
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                val desc = "Code ${error?.errorCode}: ${error?.description} on ${request?.url}"
                Log.w("YouTubePlayer", "WebView onReceivedError: $desc")
                onWebViewErrorDesc(desc)
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
                val desc = "HTTP ${errorResponse?.statusCode} (${errorResponse?.reasonPhrase}) on ${request?.url}"
                Log.w("YouTubePlayer", "WebView onReceivedHttpError: $desc")
                onHttpErrorDesc(desc)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // In Option B, listen for YouTube embed window postMessage events
                if (mode == PlaybackMode.OPTION_B_LOAD_URL) {
                    view?.evaluateJavascript(
                        """
                        (function() {
                            if (window._ytPostMessageHooked) return;
                            window._ytPostMessageHooked = true;
                            window.addEventListener('message', function(e) {
                                try {
                                    var data = typeof e.data === 'string' ? JSON.parse(e.data) : e.data;
                                    if (data) {
                                        if (data.event === 'onError' && data.info !== undefined) {
                                            AndroidBridge.onError(data.info);
                                        } else if (data.event === 'onReady') {
                                            AndroidBridge.onReady();
                                        } else if (data.event === 'onStateChange' && data.info !== undefined) {
                                            AndroidBridge.onStateChange(data.info);
                                        }
                                    }
                                } catch(err) {}
                            });
                        })();
                        """.trimIndent(),
                        null
                    )
                }
            }
        }

        // Bridge for official YouTube IFrame API communication
        val bridge = YouTubeJsInterface(
            onReadyCallback = onReady,
            onErrorCallback = onError,
            onStateChangeCallback = onStateChange
        )
        addJavascriptInterface(bridge, "AndroidBridge")

        when (mode) {
            PlaybackMode.OPTION_A_BASE_URL -> {
                // Option A: Local HTML document with loadDataWithBaseURL using real applicationId
                val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                      <meta name="referrer" content="strict-origin-when-cross-origin">
                      <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body, html { width: 100%; height: 100%; background-color: #000; overflow: hidden; }
                        #player { width: 100%; height: 100%; position: absolute; top: 0; left: 0; border: none; }
                      </style>
                    </head>
                    <body>
                      <iframe id="player"
                              type="text/html"
                              width="100%"
                              height="100%"
                              src="https://www.youtube.com/embed/$videoId?enablejsapi=1&autoplay=1&playsinline=1&controls=1&rel=0&fs=1&origin=$refererHeader&widget_referrer=$refererHeader"
                              frameborder="0"
                              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                              referrerpolicy="strict-origin-when-cross-origin"
                              allowfullscreen>
                      </iframe>
                      <script>
                        var tag = document.createElement('script');
                        tag.src = "https://www.youtube.com/iframe_api";
                        var firstScriptTag = document.getElementsByTagName('script')[0];
                        firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                        var player;
                        function onYouTubeIframeAPIReady() {
                          try {
                            player = new YT.Player('player', {
                              events: {
                                'onReady': function(event) {
                                  try { event.target.playVideo(); } catch(e){}
                                  try { AndroidBridge.onReady(); } catch(e){}
                                },
                                'onError': function(event) {
                                  try { AndroidBridge.onError(event.data); } catch(e){}
                                },
                                'onStateChange': function(event) {
                                  try { AndroidBridge.onStateChange(event.data); } catch(e){}
                                }
                              }
                            });
                          } catch(err) {
                            try { AndroidBridge.onError(5); } catch(e){}
                          }
                        }
                      </script>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL(baseUrl, htmlContent, "text/html", "UTF-8", null)
            }

            PlaybackMode.OPTION_B_LOAD_URL -> {
                // Option B: Load embed URL directly with HTTP Referer header
                val embedUrl = "https://www.youtube.com/embed/$videoId?enablejsapi=1&autoplay=1&playsinline=1&controls=1&rel=0&fs=1&origin=$refererHeader"
                val extraHeaders = mapOf(
                    "Referer" to refererHeader
                )
                loadUrl(embedUrl, extraHeaders)
            }
        }
    }
}
