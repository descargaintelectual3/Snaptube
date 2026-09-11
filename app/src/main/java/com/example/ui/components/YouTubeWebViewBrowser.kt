package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import com.example.data.model.VideoItem
import com.example.engine.YouTubeMediaInterceptorService
import com.example.ui.theme.SnaptubeRed
import com.example.ui.theme.SnaptubeYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

private const val YOUTUBE_HOME_URL = "https://m.youtube.com"

private const val AD_BLOCKER_JS = """
(function() {
    // 0. Anti-bot and genuine device fingerprint spoofing
    try {
        Object.defineProperty(navigator, 'webdriver', { get: () => false });
        window.chrome = { runtime: {}, app: {} };
        if (!navigator.languages || navigator.languages.length === 0) {
            Object.defineProperty(navigator, 'languages', { get: () => ['es-ES', 'es', 'en-US', 'en'] });
        }
    } catch(e) {}

    // 1. Inject CSS to completely hide ad banners, promoted cards, and companion popups
    var style = document.getElementById('snaptube-adblock-style');
    if (!style) {
        style = document.createElement('style');
        style.id = 'snaptube-adblock-style';
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

    // 2. High-speed auto-skip video ad loop
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

    // 3. YouTube Bot Check & Sign-in Error Auto-Unblocker
    function unlockBotBlockedVideo() {
        var url = window.location.href;
        var match = url.match(/[?&]v=([a-zA-Z0-9_-]{11})/) || url.match(/\/shorts\/([a-zA-Z0-9_-]{11})/) || url.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/);
        if (!match) return;
        var videoId = match[1];

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
                iframe.style.minHeight = '220px';
                iframe.style.border = '0';
                iframe.allow = 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share';
                iframe.allowFullscreen = true;

                playerContainer.innerHTML = '';
                playerContainer.appendChild(iframe);
                playerContainer.style.display = 'block';
                playerContainer.style.height = '220px';
                playerContainer.style.background = '#000';
            }
        }
    }

    if (!window.__snaptube_ad_skipper_installed) {
        window.__snaptube_ad_skipper_installed = true;
        setInterval(skipVideoAds, 300);
        setInterval(unlockBotBlockedVideo, 500);
    }
})();
"""

@UnstableApi
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeWebViewBrowser(
    interceptorService: YouTubeMediaInterceptorService,
    onDownloadClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentUrl by remember { mutableStateOf(YOUTUBE_HOME_URL) }
    var inputQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    val activePageUrlRef = remember { java.util.concurrent.atomic.AtomicReference(YOUTUBE_HOME_URL) }

    val detectedMedia by interceptorService.currentDetectedMedia.collectAsState()

    // Handle system back button for native-like browser navigation
    BackHandler(enabled = canGoBack || customView != null) {
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.stopLoading()
                webViewInstance?.destroy()
                webViewInstance = null
            } catch (e: Exception) {
                // Ignore disposal errors
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("youtube_webview_browser")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Native-Like YouTube Navigation Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = canGoBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Forward
                        IconButton(
                            onClick = { webViewInstance?.goForward() },
                            enabled = canGoForward,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Adelante",
                                tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Home button (m.youtube.com)
                        IconButton(
                            onClick = {
                                inputQuery = ""
                                webViewInstance?.loadUrl(YOUTUBE_HOME_URL)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Inicio YouTube",
                                tint = SnaptubeYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Search Input Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                TextField(
                                    value = inputQuery,
                                    onValueChange = { inputQuery = it },
                                    placeholder = {
                                        Text(
                                            text = "Buscar en YouTube o pegar link...",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
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
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            val query = inputQuery.trim()
                                            if (query.isNotBlank()) {
                                                val targetUrl = if (query.startsWith("http://") || query.startsWith("https://")) {
                                                    query
                                                } else {
                                                    "https://m.youtube.com/results?search_query=${URLEncoder.encode(query, "UTF-8")}"
                                                }
                                                webViewInstance?.loadUrl(targetUrl)
                                            }
                                        }
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Refresh
                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recargar",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Status Ribbon: AdBlocker Status & Protection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AdBlock Activo • 0 Anuncios",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                        }
                        Text(
                            text = "Aceleración GPU • Extractor HD",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    // Progress bar
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = SnaptubeYellow,
                            trackColor = Color.Transparent
                        )
                    }
                }
            }

            // 2. Main YouTube WebView Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            android.webkit.CookieManager.getInstance().setAcceptCookie(true)
                            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                databaseEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                cacheMode = WebSettings.LOAD_DEFAULT
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.127 Mobile Safari/537.36"
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    val reqUrl = request?.url?.toString() ?: return null
                                    val pageUrl = request.requestHeaders?.get("Referer") ?: activePageUrlRef.get()
                                    return try {
                                        interceptorService.interceptRequest(reqUrl, pageUrl)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    url?.let {
                                        currentUrl = it
                                        activePageUrlRef.set(it)
                                        interceptorService.onPageNavigation(it)
                                    }
                                    view?.evaluateJavascript(AD_BLOCKER_JS, null)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                    url?.let {
                                        currentUrl = it
                                        activePageUrlRef.set(it)
                                        interceptorService.onPageNavigation(it)
                                    }
                                    view?.evaluateJavascript(AD_BLOCKER_JS, null)
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: android.webkit.RenderProcessGoneDetail?
                                ): Boolean {
                                    view?.let {
                                        val parent = it.parent as? ViewGroup
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

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    isLoading = newProgress < 100
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                }

                                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                    customView = view
                                    customViewCallback = callback
                                }

                                override fun onHideCustomView() {
                                    customView = null
                                    customViewCallback = null
                                }
                            }

                            loadUrl(YOUTUBE_HOME_URL)
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        webViewInstance = view
                    }
                )

                // Fullscreen container when video goes fullscreen
                customView?.let { view ->
                    AndroidView(
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                addView(view)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }
            }
        }

        // 3. Floating Quick Download Pill when media is detected from YouTube WebView
        AnimatedVisibility(
            visible = detectedMedia != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            detectedMedia?.let { video ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .clickable { onDownloadClick(video) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SnaptubeYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Descargar Video",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Video Detectado (Directo)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SnaptubeYellow
                            )
                            Text(
                                text = video.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Formatos disponibles: ${video.qualityOptions.size} (1080p, 720p, MP3 HQ)",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SnaptubeYellow)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "DESCARGAR",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
