package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.DownloadQualityOption
import com.example.data.model.MediaType
import com.example.data.model.VideoItem
import com.example.data.repository.MediaCatalog
import com.example.ui.theme.SnaptubeRed
import com.example.ui.theme.SnaptubeYellow

private const val BROWSER_ANTI_BOT_AND_ADBLOCK_JS = """
(function() {
    try {
        Object.defineProperty(navigator, 'webdriver', { get: () => false });
        window.chrome = { runtime: {}, app: {} };
        if (!navigator.languages || navigator.languages.length === 0) {
            Object.defineProperty(navigator, 'languages', { get: () => ['es-ES', 'es', 'en-US', 'en'] });
        }
    } catch(e) {}

    var style = document.getElementById('snaptube-browser-adblock');
    if (!style) {
        style = document.createElement('style');
        style.id = 'snaptube-browser-adblock';
        style.innerHTML = `
            .ad-showing, .ad-interrupting, .video-ads, .ytp-ad-module,
            .ytp-ad-overlay-container, .ytp-ad-text, .ytp-ad-skip-button-slot,
            ytd-action-companion-ad-renderer, ytd-banner-promo-renderer,
            .companion-ad-container, #player-ads, .ad-container,
            ytm-promoted-sparkles-web-renderer, ytm-app-banner,
            .ytm-promoted-video-renderer, ytm-promoted-sparkles-text-search-web-renderer {
                display: none !important;
                visibility: hidden !important;
                opacity: 0 !important;
                height: 0 !important;
            }
        `;
        document.head.appendChild(style);
    }

    function skipVideoAds() {
        var video = document.querySelector('video');
        var player = document.querySelector('.html5-video-player');
        if (video && player && (player.classList.contains('ad-showing') || player.classList.contains('ad-interrupting'))) {
            video.muted = true;
            if (isFinite(video.duration) && video.duration > 0) {
                video.currentTime = video.duration;
            }
        }

        var skipBtns = document.querySelectorAll(
            '.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button, .videoAdUiSkipButton'
        );
        skipBtns.forEach(function(btn) {
            if (btn && typeof btn.click === 'function') {
                btn.click();
            }
        });
    }

    function unblockYouTubeBotCheck() {
        var url = window.location.href;
        var match = url.match(/[?&]v=([a-zA-Z0-9_-]{11})/) || url.match(/\/shorts\/([a-zA-Z0-9_-]{11})/) || url.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/);
        if (!match) return;
        var videoId = match[1];

        if (window.SnaptubeBridge && typeof window.SnaptubeBridge.onMediaFound === 'function') {
            window.SnaptubeBridge.onMediaFound(document.title || 'Video de YouTube', 'https://www.youtube.com/watch?v=' + videoId);
        }

        var bodyText = document.body ? document.body.innerText : '';
        var isBlocked = bodyText.indexOf("not a bot") !== -1 || 
                        bodyText.indexOf("no eres un robot") !== -1 ||
                        bodyText.indexOf("Sign in to confirm") !== -1 ||
                        document.querySelector('ytm-player-error-message-renderer') !== null ||
                        document.querySelector('.ytp-error-content') !== null;

        if (isBlocked && !document.getElementById('snaptube-unblocked-embed')) {
            var playerContainer = document.querySelector('ytm-player-error-message-renderer') ||
                                  document.querySelector('.player-container') ||
                                  document.getElementById('player') ||
                                  document.querySelector('#player-control-overlay') ||
                                  document.querySelector('.html5-video-player');

            if (playerContainer) {
                var iframe = document.createElement('iframe');
                iframe.id = 'snaptube-unblocked-embed';
                iframe.src = 'https://www.youtube-nocookie.com/embed/' + videoId + '?autoplay=1&playsinline=1&controls=1&rel=0&modestbranding=1';
                iframe.style.width = '100%';
                iframe.style.height = '100%';
                iframe.style.minHeight = '230px';
                iframe.style.border = '0';
                iframe.allow = 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share';
                iframe.allowFullscreen = true;

                playerContainer.innerHTML = '';
                playerContainer.appendChild(iframe);
                playerContainer.style.display = 'block';
                playerContainer.style.height = '230px';
                playerContainer.style.background = '#000';
            }
        }
    }

    if (!window.__snaptube_browser_installed) {
        window.__snaptube_browser_installed = true;
        setInterval(skipVideoAds, 300);
        setInterval(unblockYouTubeBotCheck, 400);
    }
    unblockYouTubeBotCheck();
})();
"""

private fun createVideoItemFromUrl(url: String, currentTitle: String? = null): VideoItem? {
    val videoId = when {
        url.contains("watch?v=") -> url.substringAfter("watch?v=").substringBefore("&")
        url.contains("/shorts/") -> url.substringAfter("/shorts/").substringBefore("?").substringBefore("&")
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
        else -> null
    }?.takeIf { it.isNotBlank() } ?: return null

    val cleanTitle = currentTitle?.takeIf {
        it.isNotBlank() && !it.contains("YouTube") && it != "Untitled"
    } ?: "Video de YouTube"

    return VideoItem(
        id = videoId,
        title = cleanTitle,
        channel = "YouTube",
        duration = "HD",
        viewCount = "Audio / Video HQ",
        publishedTime = "Online",
        thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
        videoUrl = "https://www.youtube.com/watch?v=$videoId",
        category = "YouTube",
        qualityOptions = MediaCatalog.getDefaultQualityOptions(24.0)
    )
}

class MediaExtractorBridge(
    private val onMediaExtracted: (title: String, mediaUrl: String) -> Unit
) {
    @JavascriptInterface
    fun onMediaFound(title: String, mediaUrl: String) {
        onMediaExtracted(title, mediaUrl)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    currentUrl: String,
    detectedMedia: VideoItem?,
    onNavigate: (String) -> Unit,
    onDownloadDetectedMedia: (VideoItem) -> Unit,
    onPlayMedia: ((VideoItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var inputUrl by remember(currentUrl) { mutableStateOf(currentUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var localDetectedMedia by remember { mutableStateOf<VideoItem?>(null) }

    val activeMedia = localDetectedMedia ?: detectedMedia ?: remember(inputUrl) {
        createVideoItemFromUrl(inputUrl)
    }

    // Pulsating animation for Snaptube download sniffer button
    val infiniteTransition = rememberInfiniteTransition(label = "sniffer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("browser_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Browser Address Bar and Nav Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (webViewInstance?.canGoBack() == true) {
                                    webViewInstance?.goBack()
                                }
                            },
                            enabled = canGoBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (webViewInstance?.canGoForward() == true) {
                                    webViewInstance?.goForward()
                                }
                            },
                            enabled = canGoForward,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Adelante",
                                tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }

                        // URL input field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Seguro SSL",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                TextField(
                                    value = inputUrl,
                                    onValueChange = { inputUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(
                                        onGo = {
                                            var target = inputUrl.trim()
                                            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                                target = if (target.contains(".")) "https://$target" else "https://www.google.com/search?q=$target"
                                            }
                                            onNavigate(target)
                                        }
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                webViewInstance?.reload() ?: onNavigate(inputUrl)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recargar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Loading indicator
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = SnaptubeYellow
                        )
                    }

                    // Quick shortcut bookmarks strip
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MediaCatalog.quickSites.filter { !it.isWaStatus }) { site ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        inputUrl = site.url
                                        onNavigate(site.url)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(site.iconColor)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = site.symbolLetter.take(1),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = site.name,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Real Android WebView rendering with JavaScript and sniffer enabled
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        webViewInstance = this

                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            allowFileAccess = true
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.127 Mobile Safari/537.36"
                        }

                        addJavascriptInterface(
                            MediaExtractorBridge { title, mediaUrl ->
                                val extracted = createVideoItemFromUrl(mediaUrl, title)
                                if (extracted != null) {
                                    localDetectedMedia = extracted
                                }
                            },
                            "SnaptubeBridge"
                        )

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                isLoading = newProgress < 100
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                url?.let {
                                    inputUrl = it
                                    val item = createVideoItemFromUrl(it)
                                    if (item != null) {
                                        localDetectedMedia = item
                                    }
                                }
                                canGoBack = view?.canGoBack() == true
                                canGoForward = view?.canGoForward() == true
                                view?.evaluateJavascript(BROWSER_ANTI_BOT_AND_ADBLOCK_JS, null)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                canGoBack = view?.canGoBack() == true
                                canGoForward = view?.canGoForward() == true
                                url?.let {
                                    val item = createVideoItemFromUrl(it, view?.title)
                                    if (item != null) {
                                        localDetectedMedia = item
                                    }
                                }
                                view?.evaluateJavascript(BROWSER_ANTI_BOT_AND_ADBLOCK_JS, null)
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
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
                        loadUrl(currentUrl)
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                    if (webView.url != currentUrl) {
                        webView.loadUrl(currentUrl)
                    }
                }
            )
        }

        // Floating Bottom Media Action Banner (Play Ad-Free & Download)
        AnimatedVisibility(
            visible = activeMedia != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 82.dp),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            activeMedia?.let { media ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("detected_media_banner"),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SnaptubeYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¡Video detectado en Snaptube!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SnaptubeYellow
                                )
                                Text(
                                    text = media.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Play in Native Snaptube Player (No Ads)
                            Button(
                                onClick = {
                                    onPlayMedia?.invoke(media)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = SnaptubeYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sin Anuncios",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Download Button
                            Button(
                                onClick = { onDownloadDetectedMedia(media) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SnaptubeYellow,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Descargar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // The Signature Snaptube Floating Pulsating Video Detector FAB
        if (activeMedia != null) {
            FloatingActionButton(
                onClick = { onDownloadDetectedMedia(activeMedia) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 170.dp)
                    .scale(pulseScale)
                    .testTag("browser_download_fab"),
                containerColor = SnaptubeYellow,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = SnaptubeRed,
                            contentColor = Color.White,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Text("1", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargar video detectado",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
