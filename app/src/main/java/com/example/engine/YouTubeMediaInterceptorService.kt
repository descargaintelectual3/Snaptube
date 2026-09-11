package com.example.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceResponse
import androidx.media3.common.util.UnstableApi
import com.example.data.model.DownloadQualityOption
import com.example.data.model.MediaType
import com.example.data.model.VideoItem
import com.example.data.repository.MediaCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Service that intercepts media requests from the embedded YouTube WebView,
 * extracts direct stream URLs for video/audio, and uses Media3 DownloadManager
 * to save files locally in high-quality formats.
 */
@UnstableApi
class YouTubeMediaInterceptorService(
    private val context: Context
) {
    private val TAG = "YouTubeInterceptor"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Currently detected media from the embedded YouTube WebView
    private val _currentDetectedMedia = MutableStateFlow<VideoItem?>(null)
    val currentDetectedMedia: StateFlow<VideoItem?> = _currentDetectedMedia.asStateFlow()

    // Cache of intercepted direct video/audio stream chunks
    private val interceptedStreams = ConcurrentHashMap<String, String>()
    private var lastVideoId: String? = null

    // Ad domains to block at the network level
    private val adDomains = listOf(
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "pubads.g.doubleclick.net",
        "securepubads.g.doubleclick.net",
        "fls-na.amazon-adsystem.com",
        "youtube.com/api/stats/ads",
        "youtube.com/pagead/",
        "youtube.com/ptracking",
        "ad.doubleclick.net",
        "static.doubleclick.net"
    )

    /**
     * Intercepts web resource requests:
     * 1. Blocks ad domains to eliminate video and display ads
     * 2. Extracts direct media stream URLs (videoplayback / googlevideo)
     */
    fun interceptRequest(requestUrl: String, pageUrl: String?): WebResourceResponse? {
        val lowerUrl = requestUrl.lowercase()

        // 1. Block advertising domains
        for (domain in adDomains) {
            if (lowerUrl.contains(domain)) {
                return WebResourceResponse(
                    "text/plain",
                    "UTF-8",
                    ByteArrayInputStream(ByteArray(0))
                )
            }
        }

        // 2. Intercept video & audio playback streams
        if (lowerUrl.contains("googlevideo.com/videoplayback") || lowerUrl.contains("videoplayback?")) {
            handleMediaPlaybackRequest(requestUrl, pageUrl)
        }

        // Allow legitimate media requests to continue normally
        return null
    }

    private fun handleMediaPlaybackRequest(streamUrl: String, pageUrl: String?) {
        try {
            val uri = Uri.parse(streamUrl)
            val itag = uri.getQueryParameter("itag") ?: ""
            val mime = uri.getQueryParameter("mime") ?: ""
            val clen = uri.getQueryParameter("clen")?.toLongOrNull() ?: 0L

            val isAudio = mime.startsWith("audio") || itag in listOf("140", "251", "171")
            val isVideo = mime.startsWith("video") || itag in listOf("18", "22", "137", "136", "298", "299")

            val currentVideo = _currentDetectedMedia.value
            if (currentVideo != null) {
                // If we already have a detected video, enrich its quality options with the genuine stream URL
                val updatedOptions = currentVideo.qualityOptions.map { opt ->
                    if ((isAudio && opt.mediaType == MediaType.AUDIO) || (isVideo && opt.mediaType == MediaType.VIDEO)) {
                        opt.copy(
                            directStreamUrl = streamUrl,
                            approximateSizeBytes = if (clen > 0) clen else opt.approximateSizeBytes
                        )
                    } else opt
                }
                _currentDetectedMedia.value = currentVideo.copy(qualityOptions = updatedOptions)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error handling media playback stream: ${e.message}")
        }
    }

    /**
     * Called when the WebView loads or navigates to a new URL
     */
    fun onPageNavigation(url: String) {
        val videoId = StreamExtractor.extractVideoId(url)
        if (videoId != null && videoId != lastVideoId) {
            lastVideoId = videoId
            Log.i(TAG, "Detected YouTube video navigation: $videoId from $url")

            // Create initial placeholder VideoItem
            val initialItem = VideoItem(
                id = videoId,
                title = "Video de YouTube",
                channel = "YouTube",
                duration = "Cargando...",
                viewCount = "Vistas",
                publishedTime = "YouTube",
                thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                videoUrl = "https://www.youtube.com/watch?v=$videoId",
                category = "YouTube",
                qualityOptions = MediaCatalog.getDefaultQualityOptions(28.0)
            )
            _currentDetectedMedia.value = initialItem

            // Resolve full high-quality formats via Innertube API
            scope.launch {
                try {
                    val resolved = StreamExtractor.resolveMedia(url)
                    if (resolved.qualityOptions.isNotEmpty()) {
                        _currentDetectedMedia.value = resolved
                        Log.i(TAG, "Successfully extracted ${resolved.qualityOptions.size} formats for $videoId")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error resolving YouTube stream formats: ${e.message}")
                }
            }
        } else if (videoId == null && !url.contains("watch") && !url.contains("shorts")) {
            // User went back to home / search results
            _currentDetectedMedia.value = null
            lastVideoId = null
        }
    }

    /**
     * Downloads the extracted direct stream locally using Media3 DownloadManager
     */
    fun downloadWithMedia3(
        video: VideoItem,
        quality: DownloadQualityOption
    ): String {
        Log.i(TAG, "Starting Media3 download for ${video.title} (${quality.qualityLabel})")
        return Media3DownloadManagerHelper.startMedia3Download(context, video, quality)
    }

    fun clearDetectedMedia() {
        _currentDetectedMedia.value = null
        lastVideoId = null
    }
}
