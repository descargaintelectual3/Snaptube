package com.example.engine

import android.content.Context
import com.example.data.model.DownloadQualityOption
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTaskEntity
import com.example.data.model.MediaType
import com.example.data.model.VideoItem
import com.example.data.repository.SnaptubeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DownloadEngine(
    private val context: Context,
    private val repository: SnaptubeRepository,
    private val scope: CoroutineScope
) {
    private val activeJobs = ConcurrentHashMap<String, Job>()
    
    private val _downloadEvent = MutableSharedFlow<String>()
    val downloadEvent = _downloadEvent.asSharedFlow()

    private val snaptubeDir: File by lazy {
        val dir = File(context.filesDir, "SnaptubeDownloads")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun startDownload(video: VideoItem, quality: DownloadQualityOption) {
        val taskId = UUID.randomUUID().toString()
        val extension = if (quality.mediaType == MediaType.AUDIO) "mp3" else "mp4"
        val cleanTitle = video.title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(40)
        val targetFile = File(snaptubeDir, "${cleanTitle}_${quality.qualityLabel}.$extension")

        val task = DownloadTaskEntity(
            id = taskId,
            title = video.title,
            sourceUrl = video.videoUrl,
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
            _downloadEvent.emit("Descarga iniciada: ${video.title.take(25)}...")
            runDownloadJob(task, targetFile)
        }
    }

    fun startDirectUrlDownload(url: String, title: String, qualityLabel: String, isAudio: Boolean) {
        val taskId = UUID.randomUUID().toString()
        val extension = if (isAudio) "mp3" else "mp4"
        val cleanTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(40)
        val targetFile = File(snaptubeDir, "${cleanTitle}_$qualityLabel.$extension")
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
            _downloadEvent.emit("Descarga iniciada desde enlace web")
            runDownloadJob(task, targetFile)
        }
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
            runDownloadJob(task, targetFile)
        }
    }

    fun cancelDownload(task: DownloadTaskEntity) {
        activeJobs[task.id]?.cancel()
        activeJobs.remove(task.id)
        scope.launch {
            repository.deleteDownloadTask(task)
            _downloadEvent.emit("Descarga cancelada")
        }
    }

    private fun runDownloadJob(task: DownloadTaskEntity, targetFile: File) {
        val job = scope.launch(Dispatchers.IO) {
            try {
                // Ensure output file exists and can be written
                if (!targetFile.exists()) {
                    targetFile.createNewFile()
                }

                val totalSize = task.totalSizeBytes.coerceAtLeast(1024 * 1024L)
                var currentBytes = task.downloadedBytes
                val chunkSize = (totalSize / 40).coerceAtLeast(64 * 1024L)

                val speeds = listOf(
                    "3.4 MB/s", "4.8 MB/s", "6.2 MB/s", "5.1 MB/s",
                    "7.5 MB/s", "4.2 MB/s", "8.1 MB/s"
                )
                var speedIndex = 0

                val fos = FileOutputStream(targetFile, true)
                val buffer = ByteArray(8192) { 0x41.toByte() }

                while (currentBytes < totalSize) {
                    delay(180)
                    currentBytes = (currentBytes + chunkSize).coerceAtMost(totalSize)
                    
                    // Write dummy buffer chunks to produce an actual file on disk
                    fos.write(buffer)

                    val percent = ((currentBytes.toDouble() / totalSize.toDouble()) * 100).toInt()
                    val speed = speeds[speedIndex % speeds.size]
                    speedIndex++

                    repository.updateDownloadTask(
                        task.copy(
                            downloadedBytes = currentBytes,
                            progressPercent = percent,
                            downloadSpeedFormatted = speed,
                            status = DownloadStatus.DOWNLOADING
                        )
                    )
                }
                fos.flush()
                fos.close()

                // Mark completed
                val completedTask = task.copy(
                    downloadedBytes = totalSize,
                    progressPercent = 100,
                    downloadSpeedFormatted = "Completado",
                    status = DownloadStatus.COMPLETED
                )
                repository.updateDownloadTask(completedTask)
                _downloadEvent.emit("¡Descarga completada! ${task.title.take(25)}")
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    repository.updateDownloadTask(
                        task.copy(
                            status = DownloadStatus.FAILED,
                            downloadSpeedFormatted = "Error de red"
                        )
                    )
                }
            } finally {
                activeJobs.remove(task.id)
            }
        }
        activeJobs[task.id] = job
    }
}
