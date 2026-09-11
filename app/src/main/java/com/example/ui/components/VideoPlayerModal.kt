package com.example.ui.components

import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.content.Intent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.data.repository.MediaCatalog
import com.example.engine.PlaybackState
import com.example.ui.theme.SnaptubeYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@UnstableApi
@Composable
fun VideoPlayerModal(
    state: PlaybackState,
    player: ExoPlayer? = null,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onForward10: () -> Unit,
    onRewind10: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onDownloadClick: (VideoItem) -> Unit,
    onToggleAudioOnly: () -> Unit = {},
    onEnterPiP: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onToggleAutoplay: () -> Unit = {},
    onSetSleepTimer: (Int?) -> Unit = {},
    onPlayItemFromQueue: (VideoItem) -> Unit = {},
    onToggleSubtitles: () -> Unit = {},
    onToggleLoop: () -> Unit = {},
    onSelectQuality: (String) -> Unit = {},
    onToggleWatchLater: ((VideoItem) -> Unit)? = null
) {
    if (!state.isFullScreenPlayerVisible || !state.isVideo) return

    val scope = rememberCoroutineScope()
    var showControls by remember { mutableStateOf(true) }
    var selectedSpeed by remember { mutableFloatStateOf(state.playbackSpeed) }
    var showDoubleTapLeftFeedback by remember { mutableStateOf(false) }
    var showDoubleTapRightFeedback by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var isSavedToWatchLater by remember { mutableStateOf(false) }
    var gestureIndicatorText by remember { mutableStateOf<String?>(null) }
    var gestureIndicatorIcon by remember { mutableStateOf<ImageVector?>(null) }

    var currentBrightness by remember { mutableFloatStateOf(state.brightnessLevel) }
    var currentVolume by remember { mutableFloatStateOf(state.volumeLevel) }
    var webViewInstance by remember { mutableStateOf<android.webkit.WebView?>(null) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF09090C)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Snaptube Player Header Bar (Limpio y Sin Anuncios)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF0F0F12))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimizar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Snaptube Brand Icon
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SnaptubeYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Snaptube Player",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Reproductor HD",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // AdBlock Shield Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Sin Anuncios",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Reload Video Button
                    IconButton(
                        onClick = { webViewInstance?.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar video",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onEnterPiP,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Pantalla flotante",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Video Screen Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                        .testTag("video_player_viewport")
                ) {
                    // Video Content or Audio-Only Visualizer
                    val effectiveLocalPath = if (state.localFilePath.isNotEmpty() && File(state.localFilePath).exists() && File(state.localFilePath).length() > 100) {
                        state.localFilePath
                    } else if (state.mediaUrl.isNotEmpty() && File(state.mediaUrl).exists() && File(state.mediaUrl).length() > 100) {
                        state.mediaUrl
                    } else {
                        ""
                    }
                    val hasLocalFile = effectiveLocalPath.isNotEmpty()
                    val isDirectStream = state.mediaUrl.isNotEmpty() && (
                        state.mediaUrl.endsWith(".mp4") || state.mediaUrl.endsWith(".mp3") || state.mediaUrl.endsWith(".m4a") ||
                        state.mediaUrl.endsWith(".webm") || state.mediaUrl.endsWith(".m3u8") || state.mediaUrl.contains("googlevideo.com")
                    )
                    val ytVideoId = remember(state.mediaUrl, state.id) {
                        com.example.engine.StreamExtractor.extractVideoId(state.mediaUrl)
                            ?: com.example.engine.StreamExtractor.extractVideoId(state.id)
                    }
                    val isOnlineYouTube = ytVideoId != null && !hasLocalFile && !isDirectStream

                    if (!state.isAudioOnlyMode) {
                        if (hasLocalFile || isDirectStream) {
                            if (player != null) {
                                // ExoPlayer hardware-accelerated surface
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { ctx ->
                                        PlayerView(ctx).apply {
                                            this.player = player
                                            useController = false
                                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                        }
                                    },
                                    update = { pv ->
                                        if (pv.player != player) {
                                            pv.player = player
                                        }
                                    }
                                )
                            } else if (hasLocalFile) {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            setVideoPath(effectiveLocalPath)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                if (state.isPlaying) start()
                                            }
                                        }
                                    },
                                    update = { vv ->
                                        if (state.isPlaying) {
                                            if (!vv.isPlaying) vv.start()
                                        } else {
                                            if (vv.isPlaying) vv.pause()
                                        }
                                    }
                                )
                            }
                        } else if (ytVideoId != null) {
                            val embedHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                    <style>
                                        * { margin:0; padding:0; box-sizing:border-box; }
                                        html, body { width:100%; height:100%; background:#000; overflow:hidden; }
                                        iframe { width:100%; height:100%; border:none; display:block; }
                                    </style>
                                    <script>
                                        try {
                                            Object.defineProperty(navigator, 'webdriver', { get: () => false });
                                            window.chrome = { runtime: {}, app: {} };
                                        } catch(e) {}
                                    </script>
                                </head>
                                <body>
                                    <iframe 
                                        id="yt-embed"
                                        src="https://www.youtube-nocookie.com/embed/$ytVideoId?autoplay=1&playsinline=1&controls=1&rel=0&modestbranding=1&enablejsapi=1&iv_load_policy=3" 
                                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                                        allowfullscreen>
                                    </iframe>
                                </body>
                                </html>
                            """.trimIndent()

                            // Reproductor Web Real con Bloqueo de Anuncios y Anti-Detección
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    android.webkit.WebView(ctx).apply {
                                        webViewInstance = this
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.databaseEnabled = true
                                        settings.mediaPlaybackRequiresUserGesture = false
                                        settings.loadWithOverviewMode = true
                                        settings.useWideViewPort = true
                                        settings.allowFileAccess = true
                                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                        
                                        webChromeClient = object : android.webkit.WebChromeClient() {
                                            override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
                                                super.onProgressChanged(view, newProgress)
                                            }
                                        }
                                        
                                        webViewClient = object : android.webkit.WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                                return false
                                            }

                                            override fun shouldInterceptRequest(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): android.webkit.WebResourceResponse? {
                                                val url = request?.url?.toString()?.lowercase() ?: return null
                                                // Bloqueo de servidores publicitarios a nivel red
                                                if (url.contains("doubleclick.net") ||
                                                    url.contains("googleads") ||
                                                    url.contains("pagead2") ||
                                                    url.contains("adservice.google") ||
                                                    url.contains("pubads.g.doubleclick") ||
                                                    url.contains("/api/stats/ads") ||
                                                    url.contains("/pagead/") ||
                                                    url.contains("fls-na.amazon-adsystem") ||
                                                    url.contains("securepubads")
                                                ) {
                                                    return android.webkit.WebResourceResponse("text/plain", "UTF-8", java.io.ByteArrayInputStream(ByteArray(0)))
                                                }
                                                return super.shouldInterceptRequest(view, request)
                                            }

                                            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                val jsAntiBot = """
                                                    (function() {
                                                        try {
                                                            Object.defineProperty(navigator, 'webdriver', { get: () => false });
                                                            window.chrome = { runtime: {}, app: {} };
                                                        } catch(e) {}
                                                    })();
                                                """.trimIndent()
                                                view?.evaluateJavascript(jsAntiBot, null)
                                            }

                                            override fun onRenderProcessGone(
                                                view: android.webkit.WebView?,
                                                detail: android.webkit.RenderProcessGoneDetail?
                                            ): Boolean {
                                                view?.let {
                                                    val parent = it.parent as? android.view.ViewGroup
                                                    parent?.removeView(it)
                                                    try {
                                                        it.destroy()
                                                    } catch (e: Exception) {
                                                        // Ignore
                                                    }
                                                }
                                                return true
                                            }
                                        }

                                        tag = ytVideoId
                                        loadDataWithBaseURL("https://www.youtube-nocookie.com", embedHtml, "text/html", "UTF-8", null)
                                    }
                                },
                                update = { wv ->
                                    webViewInstance = wv
                                    val currentTag = wv.tag as? String
                                    if (currentTag != ytVideoId) {
                                        wv.tag = ytVideoId
                                        wv.loadDataWithBaseURL("https://www.youtube-nocookie.com", embedHtml, "text/html", "UTF-8", null)
                                    }
                                }
                            )
                        } else {
                            AsyncImage(
                                model = state.thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop" },
                                contentDescription = state.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        // YouTube Premium Audio Only Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF2E2405), Color(0xFF0C0A02))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(SnaptubeYellow.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = SnaptubeYellow,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = SnaptubeYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Modo Solo Audio (Ahorro de batería y datos)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SnaptubeYellow
                                    )
                                }
                            }
                        }
                    }

                    // Buffering Indicator
                    if (state.isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center),
                            color = SnaptubeYellow,
                            strokeWidth = 3.dp
                        )
                    }

                    // Subtitles (CC) Overlay
                    if (state.isSubtitlesEnabled) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = if (showControls) 54.dp else 16.dp)
                                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (state.activeSubtitleText.isNotEmpty()) state.activeSubtitleText else "♪ ${state.title} ♪",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Gesture & Controls Overlays (Only for local files and direct streams; YouTube has its own controls)
                    if (!isOnlineYouTube) {
                        // Gesture Overlays: Left 50% for Double Tap Rewind & Brightness Drag
                        Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterStart)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { showControls = !showControls },
                                    onDoubleTap = {
                                        onRewind10()
                                        showDoubleTapLeftFeedback = true
                                        scope.launch {
                                            delay(600)
                                            showDoubleTapLeftFeedback = false
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    currentBrightness = (currentBrightness - (dragAmount / 400f)).coerceIn(0.1f, 1f)
                                    gestureIndicatorText = "Brillo: ${(currentBrightness * 100).toInt()}%"
                                    gestureIndicatorIcon = Icons.Default.BrightnessMedium
                                    scope.launch {
                                        delay(800)
                                        gestureIndicatorText = null
                                    }
                                }
                            }
                    )

                    // Gesture Overlays: Right 50% for Double Tap Forward & Volume Drag
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterEnd)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { showControls = !showControls },
                                    onDoubleTap = {
                                        onForward10()
                                        showDoubleTapRightFeedback = true
                                        scope.launch {
                                            delay(600)
                                            showDoubleTapRightFeedback = false
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    currentVolume = (currentVolume - (dragAmount / 400f)).coerceIn(0f, 1f)
                                    gestureIndicatorText = "Volumen: ${(currentVolume * 100).toInt()}%"
                                    gestureIndicatorIcon = Icons.AutoMirrored.Filled.VolumeUp
                                    scope.launch {
                                        delay(800)
                                        gestureIndicatorText = null
                                    }
                                }
                            }
                    )

                    // YouTube style double tap feedback
                    if (showDoubleTapLeftFeedback) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 32.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FastRewind, contentDescription = null, tint = SnaptubeYellow)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("-10 seg", color = SnaptubeYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    if (showDoubleTapRightFeedback) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 32.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("+10 seg", color = SnaptubeYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.FastForward, contentDescription = null, tint = SnaptubeYellow)
                            }
                        }
                    }

                    // Brightness / Volume on-screen HUD indicator
                    gestureIndicatorText?.let { text ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 48.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.8f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                gestureIndicatorIcon?.let { icon ->
                                    Icon(icon, contentDescription = null, tint = SnaptubeYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Controls Overlay
                    if (showControls) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.8f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                        )

                        // Top Bar: Minimize, Title, CC, Quality, Loop, Audio/Video Switch, PiP button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .align(Alignment.TopCenter),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Minimizar",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            // CC Subtitles Button
                            IconButton(
                                onClick = onToggleSubtitles,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isSubtitlesEnabled) SnaptubeYellow else Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Subtítulos",
                                    tint = if (state.isSubtitlesEnabled) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Loop Button
                            IconButton(
                                onClick = onToggleLoop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isLoopMode) SnaptubeYellow else Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = if (state.isLoopMode) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = "Repetir",
                                    tint = if (state.isLoopMode) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // YouTube Premium Audio/Video Toggle
                            IconButton(
                                onClick = onToggleAudioOnly,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isAudioOnlyMode) SnaptubeYellow else Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = if (state.isAudioOnlyMode) Icons.Default.Headphones else Icons.Default.Videocam,
                                    contentDescription = "Modo Audio",
                                    tint = if (state.isAudioOnlyMode) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Picture in picture button
                            IconButton(
                                onClick = onEnterPiP,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPicture,
                                    contentDescription = "Pantalla Flotante PiP",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Center Playback Controls: Rewind10, Play/Pause, Forward10, Next
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            IconButton(
                                onClick = onRewind10,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "Retroceder 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            IconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(SnaptubeYellow)
                            ) {
                                Icon(
                                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (state.isPlaying) "Pausar" else "Reproducir",
                                    tint = Color.Black,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(
                                onClick = onForward10,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "Adelantar 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            IconButton(
                                onClick = onPlayNext,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Siguiente",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Bottom Scrubber & Time
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.currentPositionFormatted,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = state.totalDurationFormatted,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Clickable Quality Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SnaptubeYellow)
                                            .clickable { showQualitySheet = true }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = state.selectedQuality,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            Slider(
                                value = state.progressFraction,
                                onValueChange = onSeek,
                                colors = SliderDefaults.colors(
                                    thumbColor = SnaptubeYellow,
                                    activeTrackColor = SnaptubeYellow,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
                }

                // Chapters Bar
                if (state.chapters.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF131318))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.chapters) { chapter ->
                            val isSelected = state.activeChapterTitle == chapter.title
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SnaptubeYellow else Color(0xFF22222A))
                                    .clickable {
                                        val frac = (chapter.startTimeSeconds.toFloat() / state.totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
                                        onSeek(frac)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = chapter.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color.LightGray
                                )
                            }
                        }
                    }
                }

                // Video Details & YouTube Channel Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = state.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${state.subtitle} • Reproducción Web Sin Anuncios",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Channel Row (Official YouTube Style)
                    var isSubscribed by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SnaptubeYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (state.subtitle.firstOrNull() ?: 'Y').toString(),
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state.subtitle.ifEmpty { "YouTube Channel" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = SnaptubeYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "1.2 M suscriptores",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { isSubscribed = !isSubscribed },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed) Color(0xFF272727) else Color(0xFFCC0000),
                                contentColor = Color.White
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isSubscribed) "SUSCRITO" else "SUSCRIBIRSE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Snaptube Action Bar (Descargar MP4 / MP3, Solo Audio, Compartir)
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Main Yellow Download Button
                        Button(
                            onClick = {
                                val currentVideo = MediaCatalog.sampleVideos.find { it.id == state.id } ?: VideoItem(
                                    id = state.id.ifEmpty { "vid_${System.currentTimeMillis()}" },
                                    title = state.title.ifEmpty { "Video" },
                                    channel = state.subtitle.ifEmpty { "Snaptube" },
                                    duration = "${(state.totalDurationSeconds / 60)}:${(state.totalDurationSeconds % 60).toString().padStart(2, '0')}",
                                    viewCount = "1.5M",
                                    publishedTime = "Reciente",
                                    thumbnailUrl = state.thumbnailUrl.ifEmpty { "https://i.ytimg.com/vi/${state.id}/hqdefault.jpg" },
                                    videoUrl = if (state.mediaUrl.startsWith("http")) state.mediaUrl else "https://www.youtube.com/watch?v=${state.id}",
                                    category = "Descargas",
                                    qualityOptions = MediaCatalog.getDefaultQualityOptions(32.0)
                                )
                                onDownloadClick(currentVideo)
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SnaptubeYellow,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Descargar MP4 / MP3",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Solo Audio Toggle
                        Button(
                            onClick = onToggleAudioOnly,
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isAudioOnlyMode) SnaptubeYellow.copy(alpha = 0.25f) else Color(0xFF1E1E24),
                                contentColor = if (state.isAudioOnlyMode) SnaptubeYellow else Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Audio",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Compartir
                        Button(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, "¡Mira este video en YouTube! ${state.mediaUrl}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Compartir video"))
                            },
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E1E24),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Compartir",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF22222A))

                // Up Next / Queue Section (YouTube Style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = SnaptubeYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "A continuación (Reproducción Sin Anuncios)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "${state.queue.size} en fila",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.queue) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPlayItemFromQueue(video)
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = video.title,
                                modifier = Modifier
                                    .size(90.dp, 54.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${video.channel} • ${video.duration}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { onDownloadClick(video) }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Descargar",
                                    tint = SnaptubeYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quality Picker Sheet
            if (showQualitySheet) {
                Dialog(onDismissRequest = { showQualitySheet = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HighQuality, contentDescription = null, tint = SnaptubeYellow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Calidad de Reproducción",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            val qualityOptions = listOf(
                                "1080p Premium (Tasa de bits mejorada)",
                                "720p 60fps HD",
                                "480p SD",
                                "360p Ahorro de datos",
                                "Solo Audio (YouTube Music)"
                            )

                            qualityOptions.forEach { q ->
                                val isSelected = state.selectedQuality.startsWith(q.take(5))
                                Button(
                                    onClick = {
                                        onSelectQuality(q)
                                        showQualitySheet = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) SnaptubeYellow else Color(0xFF282832),
                                        contentColor = if (isSelected) Color.Black else Color.White
                                    )
                                ) {
                                    Text(text = q, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Sleep Timer Picker Dialog
            if (showSleepTimerSheet) {
                Dialog(onDismissRequest = { showSleepTimerSheet = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Temporizador de Apagado",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Atenúa el volumen suavemente y detiene la reproducción",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val timerOptions = listOf(
                                "15 minutos" to 15,
                                "30 minutos" to 30,
                                "45 minutos" to 45,
                                "60 minutos (1 hora)" to 60,
                                "Al finalizar este video" to 4,
                                "Desactivar temporizador" to null
                            )

                            timerOptions.forEach { (label, minutes) ->
                                Button(
                                    onClick = {
                                        onSetSleepTimer(minutes)
                                        showSleepTimerSheet = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.sleepTimerMinutesRemaining == minutes) SnaptubeYellow else Color(0xFF282832),
                                        contentColor = if (state.sleepTimerMinutesRemaining == minutes) Color.Black else Color.White
                                    )
                                ) {
                                    Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
