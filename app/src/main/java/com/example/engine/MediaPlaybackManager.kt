package com.example.engine

import com.example.data.model.VideoItem
import com.example.data.repository.MediaCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackState(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val thumbnailUrl: String = "",
    val mediaUrl: String = "",
    val isVideo: Boolean = false,
    val isAudioOnlyMode: Boolean = false, // YouTube Premium background/audio-only
    val isPlaying: Boolean = false,
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
    val isOfflineMedia: Boolean = false
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

class MediaPlaybackManager(private val scope: CoroutineScope) {
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        // Initialize queue with default recommended catalog
        _playbackState.value = _playbackState.value.copy(
            queue = MediaCatalog.sampleVideos
        )
    }

    fun playMedia(
        id: String,
        title: String,
        subtitle: String,
        thumbnailUrl: String,
        mediaUrl: String,
        isVideo: Boolean,
        openFullScreen: Boolean = isVideo,
        localFilePath: String = ""
    ) {
        tickerJob?.cancel()
        // Ensure queue contains relevant videos around this one
        val catalog = MediaCatalog.sampleVideos
        val queueList = if (catalog.any { it.id == id }) {
            val currentIndex = catalog.indexOfFirst { it.id == id }
            catalog.drop(currentIndex + 1) + catalog.take(currentIndex)
        } else {
            catalog
        }

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
            isOfflineMedia = localFilePath.isNotEmpty()
        )
        startTicker()
    }

    fun togglePlayPause() {
        val current = _playbackState.value
        val nextPlaying = !current.isPlaying
        _playbackState.value = current.copy(isPlaying = nextPlaying)
        if (nextPlaying) {
            startTicker()
        } else {
            tickerJob?.cancel()
        }
    }

    fun seekToFraction(fraction: Float) {
        val current = _playbackState.value
        val newPos = (fraction * current.totalDurationSeconds).toInt()
        _playbackState.value = current.copy(currentPositionSeconds = newPos)
    }

    fun forward10() {
        val current = _playbackState.value
        val newPos = (current.currentPositionSeconds + 10).coerceAtMost(current.totalDurationSeconds)
        _playbackState.value = current.copy(currentPositionSeconds = newPos)
    }

    fun rewind10() {
        val current = _playbackState.value
        val newPos = (current.currentPositionSeconds - 10).coerceAtLeast(0)
        _playbackState.value = current.copy(currentPositionSeconds = newPos)
    }

    fun setSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
    }

    fun toggleRepeat() {
        _playbackState.value = _playbackState.value.copy(isRepeat = !_playbackState.value.isRepeat)
    }

    fun toggleAudioOnlyMode() {
        val current = _playbackState.value
        _playbackState.value = current.copy(isAudioOnlyMode = !current.isAudioOnlyMode)
    }

    fun toggleAutoplay() {
        val current = _playbackState.value
        _playbackState.value = current.copy(isAutoplayEnabled = !current.isAutoplayEnabled)
    }

    fun setPlaybackQuality(quality: String) {
        _playbackState.value = _playbackState.value.copy(selectedQuality = quality)
    }

    fun toggleLoopMode() {
        val current = _playbackState.value
        _playbackState.value = current.copy(isLoopMode = !current.isLoopMode)
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
        _playbackState.value = _playbackState.value.copy(
            volumeLevel = volume.coerceIn(0.0f, 1.0f)
        )
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = minutes)
        if (minutes != null && minutes > 0) {
            sleepTimerJob = scope.launch(Dispatchers.Default) {
                var remaining = minutes
                while (remaining > 0) {
                    delay(60_000)
                    remaining -= 1
                    _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = remaining)
                }
                // Sleep timer completed, stop playback
                togglePlayPause()
                _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = null)
            }
        }
    }

    fun playNextInQueue() {
        val current = _playbackState.value
        if (current.queue.isNotEmpty()) {
            val next = if (current.isShuffle) {
                current.queue.random()
            } else {
                current.queue.first()
            }
            val remainingQueue = current.queue.filter { it.id != next.id }
            playMedia(
                id = next.id,
                title = next.title,
                subtitle = next.channel,
                thumbnailUrl = next.thumbnailUrl,
                mediaUrl = next.videoUrl,
                isVideo = true,
                openFullScreen = current.isFullScreenPlayerVisible
            )
            _playbackState.value = _playbackState.value.copy(queue = remainingQueue)
        }
    }

    fun openFullScreen() {
        _playbackState.value = _playbackState.value.copy(isFullScreenPlayerVisible = true)
    }

    fun closeFullScreen() {
        _playbackState.value = _playbackState.value.copy(isFullScreenPlayerVisible = false)
    }

    fun closePlayer() {
        tickerJob?.cancel()
        sleepTimerJob?.cancel()
        _playbackState.value = PlaybackState()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                val current = _playbackState.value
                if (current.isPlaying) {
                    if (current.currentPositionSeconds >= current.totalDurationSeconds) {
                        if (current.isRepeat) {
                            _playbackState.value = current.copy(currentPositionSeconds = 0)
                        } else if (current.isAutoplayEnabled && current.queue.isNotEmpty()) {
                            // YouTube Premium Autoplay
                            launch(Dispatchers.Main) {
                                playNextInQueue()
                            }
                            break
                        } else {
                            _playbackState.value = current.copy(isPlaying = false, currentPositionSeconds = 0)
                            break
                        }
                    } else {
                        _playbackState.value = current.copy(
                            currentPositionSeconds = current.currentPositionSeconds + 1
                        )
                    }
                }
            }
        }
    }
}
