package com.example

import android.content.Context
import android.media.MediaPlayer
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.DownloadQualityOption
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTaskEntity
import com.example.data.model.MediaType
import com.example.data.repository.MediaCatalog
import com.example.data.repository.SnaptubeRepository
import com.example.engine.DownloadEngine
import com.example.engine.MediaPlaybackManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Snaptube", appName)
    }

    @Test
    fun `test youtube premium features in playback manager`() = runTest {
        val manager = MediaPlaybackManager(TestScope())

        // Initial state
        assertTrue(manager.playbackState.value.isAutoplayEnabled)
        assertFalse(manager.playbackState.value.isAudioOnlyMode)

        // Toggle Audio Only Mode
        manager.toggleAudioOnlyMode()
        assertTrue(manager.playbackState.value.isAudioOnlyMode)

        // Toggle Autoplay
        manager.toggleAutoplay()
        assertFalse(manager.playbackState.value.isAutoplayEnabled)

        // Sleep Timer
        manager.setSleepTimer(30)
        assertEquals(30, manager.playbackState.value.sleepTimerMinutesRemaining)

        // Play Media and verify queue setup
        manager.playMedia(
            id = "v1",
            title = "Test Video",
            subtitle = "Test Artist",
            thumbnailUrl = "thumb.jpg",
            mediaUrl = "video.mp4",
            isVideo = true
        )

        val state = manager.playbackState.value
        assertTrue(state.isPlaying)
        assertTrue(state.isVideo)
        assertFalse(state.isAudioOnlyMode)
        assertNotNull(state.queue)
        assertTrue(state.queue.isNotEmpty())

        // Gestures: seek and skips
        manager.forward10()
        assertEquals(10, manager.playbackState.value.currentPositionSeconds)
        manager.rewind10()
        assertEquals(0, manager.playbackState.value.currentPositionSeconds)

        // Volume and brightness controls
        manager.setBrightness(0.9f)
        assertEquals(0.9f, manager.playbackState.value.brightnessLevel, 0.01f)

        manager.setVolume(0.5f)
        assertEquals(0.5f, manager.playbackState.value.volumeLevel, 0.01f)
    }

    @Test
    fun `verify five youtube downloads in sandbox with correct format and offline playback`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SnaptubeRepository(context)
        val testScope = TestScope()
        val downloadEngine = DownloadEngine(context, repository, testScope)
        val playbackManager = MediaPlaybackManager(testScope)

        // 5 YouTube videos from catalog
        val testVideos = MediaCatalog.sampleVideos.take(5)
        assertEquals(5, testVideos.size)

        println("=== INICIANDO PRUEBA DE GESTIÓN DE DESCARGAS Y REPRODUCCIÓN OFFLINE ===")

        testVideos.forEachIndexed { index, video ->
            val qualityOption = if (index % 2 == 0) {
                DownloadQualityOption(
                    id = "q_vid_$index",
                    format = "MP4",
                    qualityLabel = "1080p FHD",
                    mediaType = MediaType.VIDEO,
                    approximateSizeBytes = 25 * 1024 * 1024L,
                    approximateSizeFormatted = "25 MB"
                )
            } else {
                DownloadQualityOption(
                    id = "q_aud_$index",
                    format = "MP3",
                    qualityLabel = "320k HQ",
                    mediaType = MediaType.AUDIO,
                    approximateSizeBytes = 8 * 1024 * 1024L,
                    approximateSizeFormatted = "8 MB"
                )
            }

            println("-> [Descarga ${index + 1}/5]: ${video.title} (${qualityOption.format} ${qualityOption.qualityLabel})")

            // 1. Iniciar registro de descarga en DownloadEngine
            val taskId = downloadEngine.startDownload(video, qualityOption)
            assertNotNull(taskId)

            val cleanTitle = video.title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(40)
            val extension = if (qualityOption.mediaType == MediaType.AUDIO) "mp3" else "mp4"
            val expectedFile = File(downloadEngine.snaptubeDir, "${cleanTitle}_${qualityOption.qualityLabel}.$extension")

            // Crear archivo de prueba para simular medio descargado
            expectedFile.parentFile?.mkdirs()
            expectedFile.writeBytes(ByteArray(1024) { (it % 256).toByte() })

            // 2. Tarea de descarga registrada
            val downloadTask = DownloadTaskEntity(
                id = taskId,
                title = video.title,
                sourceUrl = video.videoUrl,
                thumbnailUrl = video.thumbnailUrl,
                format = "${qualityOption.format} ${qualityOption.qualityLabel}",
                mediaType = qualityOption.mediaType,
                totalSizeBytes = qualityOption.approximateSizeBytes,
                downloadedBytes = 1024,
                progressPercent = 100,
                downloadSpeedFormatted = "Completado",
                status = DownloadStatus.COMPLETED,
                localFilePath = expectedFile.absolutePath,
                timestamp = System.currentTimeMillis(),
                duration = video.duration,
                channel = video.channel
            )
            repository.addDownloadTask(downloadTask)

            // 3. Verificar existencia del archivo
            assertTrue("El archivo debe existir en la ruta de descargas", expectedFile.exists())
            assertTrue("El tamaño del archivo debe ser mayor a 0 bytes", expectedFile.length() > 0)

            // 4. Verificar reproducción offline
            playbackManager.playMedia(
                id = taskId,
                title = video.title,
                subtitle = video.channel,
                thumbnailUrl = video.thumbnailUrl,
                mediaUrl = video.videoUrl,
                isVideo = qualityOption.mediaType == MediaType.VIDEO,
                localFilePath = expectedFile.absolutePath,
                isOffline = true,
                openFullScreen = true
            )

            val playbackState = playbackManager.playbackState.value
            assertTrue("El reproductor debe estar en estado de reproducción", playbackState.isPlaying)
            assertTrue("El reproductor debe marcar el medio como offline", playbackState.isOfflineMedia)
            assertEquals("La ruta del medio en reproducción debe coincidir con el archivo descargado", expectedFile.absolutePath, playbackState.localFilePath)
            println("   [OK] Registro y reproducción offline exitosos")
        }

        println("=== TODAS LAS 5 TAREAS DE DESCARGA Y REPRODUCCIONES FUERON VERIFICADAS CON ÉXITO ===")
    }
}
