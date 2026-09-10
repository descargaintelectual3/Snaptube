package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
}
