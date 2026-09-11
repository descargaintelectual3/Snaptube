package com.example.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.MediaType
import com.example.data.model.VideoChapter
import com.example.data.model.VideoItem
import com.example.data.repository.MediaCatalog
import com.example.data.repository.SnaptubeRepository
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

data class PlaybackState(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val thumbnailUrl: String = "",
    val mediaUrl: String = "",
    val isVideo: Boolean = false,
    val isAudioOnlyMode: Boolean = false, // YouTube Premium background/audio-only
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionSeconds: Int = 0,
    val totalDurationSeconds: Int = 240,
    val isMiniPlayerVisible: Boolean = false,
    val isFullScreenPlayerVisible: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val isAutoplayEnabled: Boolean = true, // YouTube Premium autoplay next video
    val selectedQuality: String = "1080p FHD", // YouTube Premium 1080p Enhanced / 4K / 720p
    val isLoopMode: Boolean = false, // YouTube Premium single-video repeat
    val isBackgroundPlaybackActive: Boolean = true, // YouTube Premium persistent audio in background
    val queue: List<VideoItem> = emptyList(),
    val sleepTimerMinutesRemaining: Int? = null, // Sleep Timer (15m, 30m, 60m)
    val brightnessLevel: Float = 0.7f,
    val volumeLevel: Float = 0.8f,
    val localFilePath: String = "",
    val isOfflineMedia: Boolean = false,
    val isSubtitlesEnabled: Boolean = false,
    val activeSubtitleText: String = "",
    val chapters: List<VideoChapter> = emptyList(),
    val activeChapterTitle: String = ""
) {
    val progressFraction: Float
        get() = if (totalDurationSeconds > 0) {
            (currentPositionSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val currentPositionFormatted: String
        get() {
            val mins = currentPositionSeconds / 60
            val secs = currentPositionSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }

    val totalDurationFormatted: String
        get() {
            val mins = totalDurationSeconds / 60
            val secs = totalDurationSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}

@UnstableApi
class MediaPlaybackManager(
    private val scope: CoroutineScope,
    private val context: Context? = null,
    private val repository: SnaptubeRepository? = null
) {
    companion object {
        private const val TAG = "MediaPlaybackManager"
    }

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressTrackerJob: Job? = null
    private var sleepTimerJob: Job? = null

    val exoPlayer: ExoPlayer? = context?.let { ctx ->
        try {
            ExoPlayer.Builder(ctx.applicationContext)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build().apply {
                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                            if (isPlaying) {
                                startProgressTracker()
                            } else {
                                progressTrackerJob?.cancel()
                            }
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_BUFFERING -> {
                                    _playbackState.value = _playbackState.value.copy(isBuffering = true)
                                }
                                Player.STATE_READY -> {
                                    val durSecs = (duration / 1000L).toInt().coerceAtLeast(1)
                                    _playbackState.value = _playbackState.value.copy(
                                        isBuffering = false,
                                        totalDurationSeconds = if (durSecs > 0) durSecs else _playbackState.value.totalDurationSeconds
                                    )
                                }
                                Player.STATE_ENDED -> {
                                    _playbackState.value = _playbackState.value.copy(isPlaying = false, isBuffering = false)
                                    if (_playbackState.value.isLoopMode) {
                                        seekToSeconds(0)
                                        play()
                                    } else if (_playbackState.value.isAutoplayEnabled) {
                                        playNext()
                                    }
                                }
                                Player.STATE_IDLE -> {
                                    _playbackState.value = _playbackState.value.copy(isBuffering = false)
                                }
                            }
                        }

                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            Log.w(TAG, "ExoPlayer playback error intercepted gracefully: ${error.message}")
                            _playbackState.value = _playbackState.value.copy(
                                isBuffering = false,
                                isPlaying = false
                            )
                        }
                    })
                }
        } catch (_: Exception) {
            null
        }
    }

    init {
        // Initialize queue with default recommended catalog
        _playbackState.value = _playbackState.value.copy(
            queue = MediaCatalog.sampleVideos
        )
    }

    private fun isDirectMediaStream(url: String): Boolean {
        if (url.isBlank()) return false
        val clean = url.lowercase().split("?")[0]
        return clean.endsWith(".mp4") ||
                clean.endsWith(".mp3") ||
                clean.endsWith(".m4a") ||
                clean.endsWith(".webm") ||
                clean.endsWith(".m3u8") ||
                clean.endsWith(".aac") ||
                clean.endsWith(".wav") ||
                clean.endsWith(".ogg") ||
                clean.endsWith(".mkv") ||
                url.contains("googlevideo.com/videoplayback")
    }

    fun playMedia(
        id: String,
        title: String,
        subtitle: String,
        thumbnailUrl: String,
        mediaUrl: String,
        isVideo: Boolean,
        openFullScreen: Boolean = isVideo,
        localFilePath: String = "",
        isOffline: Boolean = localFilePath.isNotEmpty()
    ) {
        progressTrackerJob?.cancel()

        val catalog = MediaCatalog.sampleVideos
        val queueList = if (catalog.any { it.id == id }) {
            val currentIndex = catalog.indexOfFirst { it.id == id }
            catalog.drop(currentIndex + 1) + catalog.take(currentIndex)
        } else {
            catalog
        }

        // Generate chapters for this video
        val generatedChapters = generateChaptersForVideo(title)

        _playbackState.value = PlaybackState(
            id = id,
            title = title,
            subtitle = subtitle,
            thumbnailUrl = thumbnailUrl,
            mediaUrl = mediaUrl,
            isVideo = isVideo,
            isAudioOnlyMode = false,
            isPlaying = true,
            currentPositionSeconds = 0,
            totalDurationSeconds = 240,
            isMiniPlayerVisible = true,
            isFullScreenPlayerVisible = openFullScreen,
            playbackSpeed = _playbackState.value.playbackSpeed,
            isShuffle = _playbackState.value.isShuffle,
            isRepeat = _playbackState.value.isRepeat,
            isAutoplayEnabled = _playbackState.value.isAutoplayEnabled,
            queue = queueList,
            sleepTimerMinutesRemaining = _playbackState.value.sleepTimerMinutesRemaining,
            localFilePath = localFilePath,
            isOfflineMedia = isOffline || localFilePath.isNotEmpty(),
            chapters = generatedChapters,
            activeChapterTitle = generatedChapters.firstOrNull()?.title ?: ""
        )

        // Setup ExoPlayer if available and media is a valid local file or direct stream
        exoPlayer?.let { player ->
            try {
                val effectiveLocalPath = if (localFilePath.isNotEmpty() && File(localFilePath).exists() && File(localFilePath).length() > 100) {
                    localFilePath
                } else if (mediaUrl.isNotEmpty() && File(mediaUrl).exists() && File(mediaUrl).length() > 100) {
                    mediaUrl
                } else {
                    ""
                }
                val hasLocalFile = effectiveLocalPath.isNotEmpty()
                val isDirectStream = isDirectMediaStream(mediaUrl)

                if (hasLocalFile || isDirectStream) {
                    val uri = if (hasLocalFile) {
                        Uri.fromFile(File(effectiveLocalPath))
                    } else {
                        Uri.parse(mediaUrl)
                    }

                    val mediaItem = MediaItem.Builder()
                        .setUri(uri)
                        .setMediaId(id)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(title)
                                .setArtist(subtitle)
                                .setArtworkUri(Uri.parse(thumbnailUrl))
                                .build()
                        )
                        .build()

                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.playbackParameters = player.playbackParameters.withSpeed(_playbackState.value.playbackSpeed)
                    player.playWhenReady = true
                } else {
                    // For web / YouTube pages, stop ExoPlayer initially; VideoPlayerModal will render the YouTube player
                    player.stop()
                    player.clearMediaItems()
                    _playbackState.value = _playbackState.value.copy(isBuffering = false)

                    // Asynchronously resolve genuine direct media stream to upgrade to native ExoPlayer
                    if (mediaUrl.contains("youtube.com") || mediaUrl.contains("youtu.be")) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val resolvedItem = StreamExtractor.resolveMedia(mediaUrl)
                                val directOpt = resolvedItem.qualityOptions.firstOrNull { it.mediaType == MediaType.VIDEO && it.directStreamUrl.isNotBlank() }
                                    ?: resolvedItem.qualityOptions.firstOrNull { it.directStreamUrl.isNotBlank() }
                                val directUrl = directOpt?.directStreamUrl ?: ""
                                if (directUrl.isNotBlank() && isDirectMediaStream(directUrl)) {
                                    _playbackState.value = _playbackState.value.copy(
                                        mediaUrl = directUrl,
                                        title = if (title.isBlank() || title.startsWith("YouTube Video")) resolvedItem.title else title
                                    )
                                    val streamMediaItem = MediaItem.Builder()
                                        .setUri(Uri.parse(directUrl))
                                        .setMediaId(id)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle(title)
                                                .setArtist(subtitle)
                                                .setArtworkUri(Uri.parse(thumbnailUrl))
                                                .build()
                                        )
                                        .build()
                                    launch(Dispatchers.Main) {
                                        player.setMediaItem(streamMediaItem)
                                        player.prepare()
                                        player.playbackParameters = player.playbackParameters.withSpeed(_playbackState.value.playbackSpeed)
                                        player.playWhenReady = true
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Direct stream auto-upgrade error: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error setting up ExoPlayer: ${e.message}")
            }
        }

        startProgressTracker()
    }

    private fun generateChaptersForVideo(title: String): List<VideoChapter> {
        return listOf(
            VideoChapter("0:00 - Introducción", 0, "0:00"),
            VideoChapter("0:45 - Lo más destacado", 45, "0:45"),
            VideoChapter("1:30 - Momento clave", 90, "1:30"),
            VideoChapter("2:40 - Clímax y desarrollo", 160, "2:40"),
            VideoChapter("3:30 - Cierre y créditos", 210, "3:30")
        )
    }

    fun play() {
        if (exoPlayer != null) {
            exoPlayer.play()
        } else {
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            startProgressTracker()
        }
    }

    fun pause() {
        if (exoPlayer != null) {
            exoPlayer.pause()
        } else {
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
            progressTrackerJob?.cancel()
        }
    }

    fun togglePlayPause() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        } else {
            val current = _playbackState.value
            val nextPlaying = !current.isPlaying
            _playbackState.value = current.copy(isPlaying = nextPlaying)
            if (nextPlaying) {
                startProgressTracker()
            } else {
                progressTrackerJob?.cancel()
            }
        }
    }

    fun seekToFraction(fraction: Float) {
        val current = _playbackState.value
        val newPosSecs = (fraction * current.totalDurationSeconds).toInt()
        seekToSeconds(newPosSecs)
    }

    fun seekToSeconds(seconds: Int) {
        val current = _playbackState.value
        val clamped = seconds.coerceIn(0, current.totalDurationSeconds)
        if (exoPlayer != null) {
            exoPlayer.seekTo(clamped * 1000L)
        }
        _playbackState.value = current.copy(currentPositionSeconds = clamped)
        updateActiveChapter(clamped)
    }

    fun forward10() {
        val current = _playbackState.value
        val newPos = (current.currentPositionSeconds + 10).coerceAtMost(current.totalDurationSeconds)
        seekToSeconds(newPos)
    }

    fun rewind10() {
        val current = _playbackState.value
        val newPos = (current.currentPositionSeconds - 10).coerceAtLeast(0)
        seekToSeconds(newPos)
    }

    fun setSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
        exoPlayer?.shuffleModeEnabled = _playbackState.value.isShuffle
    }

    fun toggleRepeat() {
        val nextRepeat = !_playbackState.value.isRepeat
        _playbackState.value = _playbackState.value.copy(isRepeat = nextRepeat)
        exoPlayer?.repeatMode = if (nextRepeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    fun toggleAudioOnlyMode() {
        val current = _playbackState.value
        val nextMode = !current.isAudioOnlyMode
        _playbackState.value = current.copy(isAudioOnlyMode = nextMode)
        exoPlayer?.let { player ->
            try {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, nextMode)
                    .build()
            } catch (_: Exception) {}
        }
    }

    fun toggleAutoplay() {
        val current = _playbackState.value
        _playbackState.value = current.copy(isAutoplayEnabled = !current.isAutoplayEnabled)
    }

    fun setPlaybackQuality(quality: String) {
        _playbackState.value = _playbackState.value.copy(selectedQuality = quality)
        exoPlayer?.let { player ->
            try {
                val paramsBuilder = player.trackSelectionParameters.buildUpon()
                when {
                    quality.contains("Audio", ignoreCase = true) -> {
                        paramsBuilder.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
                    }
                    quality.contains("4K", ignoreCase = true) -> {
                        paramsBuilder.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                            .setMaxVideoSize(3840, 2160)
                    }
                    quality.contains("1080p", ignoreCase = true) -> {
                        paramsBuilder.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                            .setMaxVideoSize(1920, 1080)
                    }
                    quality.contains("720p", ignoreCase = true) -> {
                        paramsBuilder.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                            .setMaxVideoSize(1280, 720)
                    }
                    else -> {
                        paramsBuilder.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                            .setMaxVideoSize(854, 480)
                    }
                }
                player.trackSelectionParameters = paramsBuilder.build()
            } catch (_: Exception) {}
        }
    }

    fun toggleSubtitles() {
        val current = _playbackState.value
        val nextEnabled = !current.isSubtitlesEnabled
        _playbackState.value = current.copy(
            isSubtitlesEnabled = nextEnabled,
            activeSubtitleText = if (nextEnabled) "Subtítulos activados (Español)" else ""
        )
    }

    fun toggleLoopMode() {
        val current = _playbackState.value
        val nextLoop = !current.isLoopMode
        _playbackState.value = current.copy(isLoopMode = nextLoop)
        exoPlayer?.repeatMode = if (nextLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun toggleBackgroundPlayback() {
        val current = _playbackState.value
        _playbackState.value = current.copy(isBackgroundPlaybackActive = !current.isBackgroundPlaybackActive)
    }

    fun setBrightness(brightness: Float) {
        _playbackState.value = _playbackState.value.copy(
            brightnessLevel = brightness.coerceIn(0.1f, 1.0f)
        )
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        _playbackState.value = _playbackState.value.copy(
            volumeLevel = clamped
        )
        exoPlayer?.volume = clamped
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = minutes)
        if (minutes != null && minutes > 0) {
            sleepTimerJob = scope.launch(Dispatchers.Default) {
                var remaining = minutes
                while (remaining > 0 && isActive) {
                    delay(60_000L)
                    remaining--
                    _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = remaining)
                }
                // Smooth audio fade out before pause
                fadeVolumeOutAndPause()
            }
        }
    }

    private suspend fun fadeVolumeOutAndPause() {
        val initialVol = _playbackState.value.volumeLevel
        for (step in 10 downTo 0) {
            val stepVol = initialVol * (step / 10f)
            setVolume(stepVol)
            delay(150L)
        }
        pause()
        setVolume(initialVol)
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = null)
    }

    fun dismissPlayer() {
        progressTrackerJob?.cancel()
        sleepTimerJob?.cancel()
        exoPlayer?.stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isMiniPlayerVisible = false,
            isFullScreenPlayerVisible = false
        )
    }

    fun closePlayer() = dismissPlayer()
    fun playNextInQueue() = playNext()

    fun openFullScreen() {
        _playbackState.value = _playbackState.value.copy(
            isFullScreenPlayerVisible = true
        )
    }

    fun closeFullScreen() {
        _playbackState.value = _playbackState.value.copy(
            isFullScreenPlayerVisible = false,
            isMiniPlayerVisible = true
        )
    }

    fun playNext() {
        val currentQueue = _playbackState.value.queue
        if (currentQueue.isNotEmpty()) {
            val nextVideo = if (_playbackState.value.isShuffle) {
                currentQueue.random()
            } else {
                currentQueue.first()
            }
            val remainingQueue = currentQueue.filter { it.id != nextVideo.id }
            playMedia(
                id = nextVideo.id,
                title = nextVideo.title,
                subtitle = nextVideo.channel,
                thumbnailUrl = nextVideo.thumbnailUrl,
                mediaUrl = nextVideo.videoUrl,
                isVideo = true,
                openFullScreen = _playbackState.value.isFullScreenPlayerVisible
            )
            _playbackState.value = _playbackState.value.copy(queue = remainingQueue)
        }
    }

    fun playPrevious() {
        seekToSeconds(0)
    }

    fun playFromQueue(video: VideoItem) {
        val currentQueue = _playbackState.value.queue.filter { it.id != video.id }
        playMedia(
            id = video.id,
            title = video.title,
            subtitle = video.channel,
            thumbnailUrl = video.thumbnailUrl,
            mediaUrl = video.videoUrl,
            isVideo = true,
            openFullScreen = _playbackState.value.isFullScreenPlayerVisible
        )
        _playbackState.value = _playbackState.value.copy(queue = currentQueue)
    }

    private fun updateActiveChapter(currentSecs: Int) {
        val chapters = _playbackState.value.chapters
        val currentChapter = chapters.lastOrNull { it.startTimeSeconds <= currentSecs }
        if (currentChapter != null && currentChapter.title != _playbackState.value.activeChapterTitle) {
            _playbackState.value = _playbackState.value.copy(activeChapterTitle = currentChapter.title)
        }
    }

    private fun startProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = scope.launch(Dispatchers.Main) {
            var counter = 0
            while (isActive) {
                val current = _playbackState.value
                if (!current.isPlaying) break

                val newPos = if (exoPlayer != null && exoPlayer.isPlaying) {
                    (exoPlayer.currentPosition / 1000L).toInt()
                } else {
                    current.currentPositionSeconds + 1
                }

                val dur = if (exoPlayer != null && exoPlayer.duration > 0) {
                    (exoPlayer.duration / 1000L).toInt()
                } else {
                    current.totalDurationSeconds
                }

                if (newPos >= dur && dur > 0) {
                    _playbackState.value = current.copy(
                        currentPositionSeconds = dur,
                        isPlaying = false
                    )
                    if (current.isLoopMode) {
                        seekToSeconds(0)
                        play()
                    } else if (current.isAutoplayEnabled) {
                        playNext()
                    }
                    break
                } else {
                    _playbackState.value = current.copy(
                        currentPositionSeconds = newPos,
                        totalDurationSeconds = dur
                    )
                    updateActiveChapter(newPos)
                }

                // Record progress to Room Watch History every 4 seconds
                counter++
                if (counter % 4 == 0 && repository != null && current.id.isNotEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        repository.recordWatchProgress(
                            videoId = current.id,
                            title = current.title,
                            channel = current.subtitle,
                            thumbnailUrl = current.thumbnailUrl,
                            videoUrl = current.mediaUrl,
                            positionSeconds = newPos,
                            durationSeconds = dur
                        )
                    }
                }

                delay(1000L)
            }
        }
    }

    fun release() {
        progressTrackerJob?.cancel()
        sleepTimerJob?.cancel()
        exoPlayer?.release()
    }
}
