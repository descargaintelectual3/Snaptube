package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.data.repository.MediaCatalog
import com.example.engine.PlaybackState
import com.example.ui.theme.SnaptubeYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerModal(
    state: PlaybackState,
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
    onPlayItemFromQueue: (VideoItem) -> Unit = {}
) {
    if (!state.isFullScreenPlayerVisible || !state.isVideo) return

    val scope = rememberCoroutineScope()
    var showControls by remember { mutableStateOf(true) }
    var selectedSpeed by remember { mutableFloatStateOf(state.playbackSpeed) }
    var showDoubleTapLeftFeedback by remember { mutableStateOf(false) }
    var showDoubleTapRightFeedback by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var gestureIndicatorText by remember { mutableStateOf<String?>(null) }
    var gestureIndicatorIcon by remember { mutableStateOf<ImageVector?>(null) }

    var currentBrightness by remember { mutableFloatStateOf(state.brightnessLevel) }
    var currentVolume by remember { mutableFloatStateOf(state.volumeLevel) }

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
                // Video Screen Area with Gesture Detection (Double Tap + Vertical Sliders)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                        .testTag("video_player_viewport")
                ) {
                    // Video Content or Audio-Only Visualizer
                    if (!state.isAudioOnlyMode) {
                        AsyncImage(
                            model = state.thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop" },
                            contentDescription = state.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
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
                                            Color.Black.copy(alpha = 0.75f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                        )

                        // Top Bar: Minimize, Title, Audio/Video Switch, PiP button
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            // YouTube Premium Audio/Video Toggle
                            IconButton(
                                onClick = onToggleAudioOnly,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isAudioOnlyMode) SnaptubeYellow else Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = if (state.isAudioOnlyMode) Icons.Default.Headphones else Icons.Default.Videocam,
                                    contentDescription = "Modo Audio",
                                    tint = if (state.isAudioOnlyMode) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Picture in picture button
                            IconButton(
                                onClick = onEnterPiP,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPicture,
                                    contentDescription = "Pantalla Flotante PiP",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
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
                                horizontalArrangement = Arrangement.SpaceBetween
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
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SnaptubeYellow)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "1080p FHD",
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

                // Premium Banner & Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // YouTube Premium Feature Tag
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1C12))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = SnaptubeYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PREMIUM • Sin Publicidad • Reproducción en Fondo Activada",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SnaptubeYellow
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = state.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.subtitle} • Calidad de Estudio",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Premium Tools Row: Download, Sleep Timer, Autoplay Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val item = MediaCatalog.sampleVideos.find { it.id == state.id }
                                    ?: MediaCatalog.sampleVideos.first()
                                onDownloadClick(item)
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SnaptubeYellow,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Descargar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Sleep Timer Button
                        Button(
                            onClick = { showSleepTimerSheet = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.sleepTimerMinutesRemaining != null) SnaptubeYellow.copy(alpha = 0.2f) else Color(0xFF1E1E24),
                                contentColor = if (state.sleepTimerMinutesRemaining != null) SnaptubeYellow else Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.LockClock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.sleepTimerMinutesRemaining != null) "${state.sleepTimerMinutesRemaining}m" else "Dormir",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Autoplay switch pill
                        Card(
                            modifier = Modifier
                                .weight(1.2f)
                                .clickable { onToggleAutoplay() },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Autoplay",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isAutoplayEnabled) SnaptubeYellow else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = state.isAutoplayEnabled,
                                    onCheckedChange = { onToggleAutoplay() },
                                    modifier = Modifier.size(32.dp, 20.dp),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SnaptubeYellow,
                                        checkedTrackColor = Color(0xFF3B2F04)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Speed selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                        speeds.forEach { speed ->
                            val isSel = selectedSpeed == speed
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SnaptubeYellow.copy(alpha = 0.3f) else Color(0xFF1B1B20))
                                    .clickable {
                                        selectedSpeed = speed
                                        onSetSpeed(speed)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${speed}x",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) SnaptubeYellow else Color.White
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF22222A))

                // Up Next / Queue Section (YouTube Style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            text = "A continuación (Cola Premium)",
                            fontSize = 14.sp,
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
                                text = "Detiene la reproducción automáticamente para ahorrar batería",
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
