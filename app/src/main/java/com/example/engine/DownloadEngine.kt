package com.example.engine

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.DownloadQualityOption
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTaskEntity
import com.example.data.model.MediaType
import com.example.data.model.VideoItem
import com.example.data.repository.SnaptubeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

/**
 * Production-grade download engine supporting multi-format streaming,
 * resumable Range requests, ID3 tagging for MP3, and MediaStore scanning.
 */
class DownloadEngine(
    private val context: Context,
    private val repository: SnaptubeRepository,
    private val scope: CoroutineScope
) {
    private val TAG = "DownloadEngine"
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val _downloadEvent = MutableSharedFlow<String>()
    val downloadEvent = _downloadEvent.asSharedFlow()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val snaptubeDir: File by lazy {
        val externalDownloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val dir = if (externalDownloads != null && externalDownloads.exists()) {
            File(externalDownloads, "Snaptube")
        } else {
            File(context.filesDir, "SnaptubeDownloads")
        }
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun startDownload(video: VideoItem, quality: DownloadQualityOption): String {
        val taskId = UUID.randomUUID().toString()
        val extension = when {
            quality.format.contains("MP3", ignoreCase = true) -> "mp3"
            quality.format.contains("M4A", ignoreCase = true) || quality.format.contains("AAC", ignoreCase = true) -> "m4a"
            quality.format.contains("WEBM", ignoreCase = true) -> "webm"
            else -> "mp4"
        }
        val cleanTitle = video.title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(40)
        val cleanQuality = quality.qualityLabel.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val targetFile = File(snaptubeDir, "${cleanTitle}_$cleanQuality.$extension")

        val effectiveStreamUrl = quality.directStreamUrl.takeIf { it.isNotBlank() } ?: video.videoUrl

        val task = DownloadTaskEntity(
            id = taskId,
            title = video.title,
            sourceUrl = effectiveStreamUrl,
            thumbnailUrl = video.thumbnailUrl,
            format = "${quality.format} ${quality.qualityLabel}",
            mediaType = quality.mediaType,
            totalSizeBytes = quality.approximateSizeBytes,
            downloadedBytes = 0,
            progressPercent = 0,
            downloadSpeedFormatted = "Conectando...",
            status = DownloadStatus.DOWNLOADING,
            localFilePath = targetFile.absolutePath,
            timestamp = System.currentTimeMillis(),
            duration = video.duration,
            channel = video.channel
        )

        scope.launch {
            repository.addDownloadTask(task)
            _downloadEvent.emit("Descargando ${quality.format} (${quality.qualityLabel}): ${video.title.take(22)}...")
            runDownloadJob(task, targetFile, effectiveStreamUrl)
        }
        return taskId
    }

    fun startDirectUrlDownload(url: String, title: String, qualityLabel: String, isAudio: Boolean): String {
        val taskId = UUID.randomUUID().toString()
        val extension = if (isAudio) "mp3" else "mp4"
        val cleanTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(40)
        val targetFile = File(snaptubeDir, "${cleanTitle}_${qualityLabel.replace(" ", "_")}.$extension")
        val sizeBytes = if (isAudio) 8 * 1024 * 1024L else 45 * 1024 * 1024L

        val task = DownloadTaskEntity(
            id = taskId,
            title = title,
            sourceUrl = url,
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop",
            format = if (isAudio) "MP3 $qualityLabel" else "MP4 $qualityLabel",
            mediaType = if (isAudio) MediaType.AUDIO else MediaType.VIDEO,
            totalSizeBytes = sizeBytes,
            downloadedBytes = 0,
            progressPercent = 0,
            downloadSpeedFormatted = "Conectando...",
            status = DownloadStatus.DOWNLOADING,
            localFilePath = targetFile.absolutePath,
            timestamp = System.currentTimeMillis(),
            duration = "4:20",
            channel = "Web Stream"
        )

        scope.launch {
            repository.addDownloadTask(task)
            _downloadEvent.emit("Iniciando descarga en formato ${task.format}")
            runDownloadJob(task, targetFile, url)
        }
        return taskId
    }

    fun pauseDownload(task: DownloadTaskEntity) {
        activeJobs[task.id]?.cancel()
        activeJobs.remove(task.id)
        scope.launch {
            repository.updateDownloadTask(
                task.copy(
                    status = DownloadStatus.PAUSED,
                    downloadSpeedFormatted = "Pausado"
                )
            )
        }
    }

    fun resumeDownload(task: DownloadTaskEntity) {
        val targetFile = File(task.localFilePath)
        scope.launch {
            repository.updateDownloadTask(
                task.copy(
                    status = DownloadStatus.DOWNLOADING,
                    downloadSpeedFormatted = "Reanudando..."
                )
            )
            runDownloadJob(task, targetFile, task.sourceUrl)
        }
    }

    fun cancelDownload(task: DownloadTaskEntity) {
        activeJobs[task.id]?.cancel()
        activeJobs.remove(task.id)
        scope.launch {
            repository.deleteDownloadTask(task)
            val file = File(task.localFilePath)
            if (file.exists()) file.delete()
            _downloadEvent.emit("Descarga cancelada")
        }
    }

    suspend fun executeDownloadSynchronously(task: DownloadTaskEntity): Boolean = withContext(Dispatchers.IO) {
        val targetFile = File(task.localFilePath)
        downloadStreamToFile(task, targetFile, task.sourceUrl)
    }

    private fun runDownloadJob(task: DownloadTaskEntity, targetFile: File, directStreamUrl: String) {
        val job = scope.launch(Dispatchers.IO) {
            try {
                val success = downloadStreamToFile(task, targetFile, directStreamUrl)
                if (success && targetFile.exists() && targetFile.length() > 0) {
                    val finalSize = targetFile.length()

                    // If it's MP3 audio, inject authentic ID3 tags and cover art
                    if (task.mediaType == MediaType.AUDIO && targetFile.name.endsWith(".mp3", ignoreCase = true)) {
                        MediaTagEngine.injectId3v2Tags(
                            targetMp3File = targetFile,
                            title = task.title,
                            artist = task.channel,
                            album = "Snaptube Downloads",
                            thumbnailUrl = task.thumbnailUrl,
                            httpClient = httpClient
                        )
                    }

                    // Register with Android MediaStore so Gallery & Music players index it
                    try {
                        val mimeType = if (task.mediaType == MediaType.AUDIO) "audio/mpeg" else "video/mp4"
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(targetFile.absolutePath),
                            arrayOf(mimeType),
                            null
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "MediaScanner error: ${e.message}")
                    }

                    val completedTask = task.copy(
                        downloadedBytes = finalSize,
                        totalSizeBytes = finalSize,
                        progressPercent = 100,
                        downloadSpeedFormatted = "Completado",
                        status = DownloadStatus.COMPLETED
                    )
                    repository.updateDownloadTask(completedTask)
                    _downloadEvent.emit("¡Descarga lista en ${task.format}! ${task.title.take(24)}")
                } else {
                    repository.updateDownloadTask(
                        task.copy(
                            status = DownloadStatus.FAILED,
                            downloadSpeedFormatted = "Error de red"
                        )
                    )
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, "Download job error: ${e.message}", e)
                    repository.updateDownloadTask(
                        task.copy(
                            status = DownloadStatus.FAILED,
                            downloadSpeedFormatted = "Error: ${e.message?.take(20)}"
                        )
                    )
                }
            } finally {
                activeJobs.remove(task.id)
            }
        }
        activeJobs[task.id] = job
    }

    private suspend fun downloadStreamToFile(
        task: DownloadTaskEntity,
        targetFile: File,
        streamUrl: String
    ): Boolean = withContext(Dispatchers.IO) {
        var resolvedUrl = streamUrl

        // If the URL is a YouTube page or missing direct stream, resolve via StreamExtractor
        if (resolvedUrl.contains("youtube.com") || resolvedUrl.contains("youtu.be") || !resolvedUrl.startsWith("http")) {
            val resolvedItem = StreamExtractor.resolveMedia(resolvedUrl)
            val matchedOption = if (task.mediaType == MediaType.AUDIO) {
                resolvedItem.qualityOptions.firstOrNull { it.mediaType == MediaType.AUDIO && it.directStreamUrl.isNotBlank() }
            } else {
                resolvedItem.qualityOptions.firstOrNull { it.mediaType == MediaType.VIDEO && it.directStreamUrl.isNotBlank() }
            }
            resolvedUrl = matchedOption?.directStreamUrl ?: ""
        }

        // If direct stream URL is empty or points to a webpage, we cannot download pure media
        if (resolvedUrl.isBlank() || resolvedUrl.contains("youtube.com/watch") || resolvedUrl.contains("youtu.be/")) {
            Log.w(TAG, "No direct media stream URL available for ${task.title}")
            return@withContext false
        }

        var downloadedFromNetwork = false
        val existingBytes = if (targetFile.exists()) targetFile.length() else 0L

        try {
            val requestBuilder = Request.Builder()
                .url(resolvedUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")

            // Support resumable Range requests if we already have partial bytes
            if (existingBytes > 0) {
                requestBuilder.header("Range", "bytes=$existingBytes-")
            }

            val request = requestBuilder.build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val body = response.body!!
                    val contentType = (response.header("Content-Type") ?: body.contentType()?.toString() ?: "").lowercase()

                    // Ensure this is a real binary media stream, NEVER an HTML error or bot verification page
                    if (contentType.contains("text/html") || contentType.contains("text/plain") || contentType.contains("application/json")) {
                        Log.w(TAG, "Refusing to write non-media Content-Type ($contentType) to ${targetFile.name}")
                        return@withContext false
                    }

                    val isAppend = response.code == 206 // 206 Partial Content
                    val contentLength = body.contentLength()
                    val totalFromNet = if (isAppend) existingBytes + contentLength else if (contentLength > 0) contentLength else task.totalSizeBytes

                    targetFile.parentFile?.mkdirs()
                    val fos = FileOutputStream(targetFile, isAppend)
                    val input: InputStream = body.byteStream()

                    val buffer = ByteArray(65536) // 64KB buffer for high throughput
                    var bytesRead: Int
                    var currentDownloaded = if (isAppend) existingBytes else 0L
                    var lastUpdateTime = System.currentTimeMillis()
                    var bytesSinceLastUpdate = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                        currentDownloaded += bytesRead
                        bytesSinceLastUpdate += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastUpdateTime >= 300) {
                            val elapsedSec = (now - lastUpdateTime) / 1000.0
                            val speedKb = if (elapsedSec > 0) (bytesSinceLastUpdate / 1024.0) / elapsedSec else 0.0
                            val speedFormatted = if (speedKb > 1024) "%.1f MB/s".format(speedKb / 1024.0) else "%.0f KB/s".format(speedKb)
                            val percent = if (totalFromNet > 0) {
                                ((currentDownloaded.toDouble() / totalFromNet.toDouble()) * 100).toInt().coerceIn(0, 99)
                            } else 50

                            repository.updateDownloadTask(
                                task.copy(
                                    downloadedBytes = currentDownloaded,
                                    totalSizeBytes = totalFromNet,
                                    progressPercent = percent,
                                    downloadSpeedFormatted = speedFormatted,
                                    status = DownloadStatus.DOWNLOADING
                                )
                            )
                            lastUpdateTime = now
                            bytesSinceLastUpdate = 0
                        }
                    }
                    fos.flush()
                    fos.close()
                    downloadedFromNetwork = targetFile.exists() && targetFile.length() > 0
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network stream failed: ${e.message}")
            downloadedFromNetwork = false
        }

        // If network download failed or file is suspiciously small, clean up
        if (!downloadedFromNetwork || !targetFile.exists() || targetFile.length() < 1024L) {
            if (targetFile.exists()) {
                targetFile.delete()
            }
            return@withContext false
        }

        return@withContext true
    }

    fun getShareIntent(task: DownloadTaskEntity): Intent? {
        val file = File(task.localFilePath)
        if (!file.exists()) return null
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mimeType = if (task.mediaType == MediaType.AUDIO) "audio/*" else "video/*"
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, task.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun getOpenWithIntent(task: DownloadTaskEntity): Intent? {
        val file = File(task.localFilePath)
        if (!file.exists()) return null
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mimeType = if (task.mediaType == MediaType.AUDIO) "audio/*" else "video/*"
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
