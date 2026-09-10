package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTaskEntity
import com.example.data.model.MediaType
import com.example.data.model.WatchHistoryEntity
import com.example.ui.theme.MusicPurple
import com.example.ui.theme.SnaptubeRed
import com.example.ui.theme.SnaptubeYellow
import com.example.ui.theme.VideoBlue
import com.example.ui.viewmodel.MyFilesTab
import androidx.compose.material.icons.filled.History

@Composable
fun MyFilesScreen(
    currentTab: MyFilesTab,
    onTabSelected: (MyFilesTab) -> Unit,
    allDownloads: List<DownloadTaskEntity>,
    watchHistory: List<WatchHistoryEntity> = emptyList(),
    onPlayItem: (DownloadTaskEntity) -> Unit,
    onPlayHistoryItem: ((WatchHistoryEntity) -> Unit)? = null,
    onClearHistory: (() -> Unit)? = null,
    onDeleteHistoryItem: ((String) -> Unit)? = null,
    onPauseTask: (DownloadTaskEntity) -> Unit,
    onResumeTask: (DownloadTaskEntity) -> Unit,
    onCancelTask: (DownloadTaskEntity) -> Unit,
    onDeleteTask: (DownloadTaskEntity) -> Unit,
    onShareItem: ((DownloadTaskEntity) -> Unit)? = null,
    onOpenWithItem: ((DownloadTaskEntity) -> Unit)? = null,
    onToggleVault: (DownloadTaskEntity) -> Unit,
    onOpenVault: () -> Unit,
    onGoToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDownloads = allDownloads.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PAUSED }
    val completedAll = allDownloads.filter { it.status == DownloadStatus.COMPLETED && !it.isInVault }
    val completedMusic = completedAll.filter { it.mediaType == MediaType.AUDIO }
    val completedVideos = completedAll.filter { it.mediaType == MediaType.VIDEO }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("my_files_screen")
    ) {
        // Sub Tabs Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            ScrollableTabRow(
                selectedTabIndex = currentTab.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                        color = SnaptubeYellow,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = currentTab == MyFilesTab.DOWNLOADING,
                    onClick = { onTabSelected(MyFilesTab.DOWNLOADING) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Descargando",
                                fontWeight = if (currentTab == MyFilesTab.DOWNLOADING) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (activeDownloads.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SnaptubeRed)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${activeDownloads.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                )

                Tab(
                    selected = currentTab == MyFilesTab.ALL,
                    onClick = { onTabSelected(MyFilesTab.ALL) },
                    text = {
                        Text(
                            text = "Todos (${completedAll.size})",
                            fontWeight = if (currentTab == MyFilesTab.ALL) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )

                Tab(
                    selected = currentTab == MyFilesTab.MUSIC,
                    onClick = { onTabSelected(MyFilesTab.MUSIC) },
                    text = {
                        Text(
                            text = "Música (${completedMusic.size})",
                            fontWeight = if (currentTab == MyFilesTab.MUSIC) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )

                Tab(
                    selected = currentTab == MyFilesTab.VIDEOS,
                    onClick = { onTabSelected(MyFilesTab.VIDEOS) },
                    text = {
                        Text(
                            text = "Videos (${completedVideos.size})",
                            fontWeight = if (currentTab == MyFilesTab.VIDEOS) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )

                Tab(
                    selected = currentTab == MyFilesTab.HISTORY,
                    onClick = { onTabSelected(MyFilesTab.HISTORY) },
                    text = {
                        Text(
                            text = "Historial (${watchHistory.size})",
                            fontWeight = if (currentTab == MyFilesTab.HISTORY) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )

                Tab(
                    selected = currentTab == MyFilesTab.VAULT,
                    onClick = { onOpenVault() },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = SnaptubeYellow
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bóveda",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }
        }

        // Body content based on selected tab
        when (currentTab) {
            MyFilesTab.DOWNLOADING -> {
                if (activeDownloads.isEmpty()) {
                    EmptyState(
                        title = "No hay descargas activas",
                        subtitle = "Explora videos y música para comenzar a descargar",
                        actionText = "Buscar contenido",
                        onAction = onGoToHome
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeDownloads, key = { it.id }) { task ->
                            DownloadingTaskCard(
                                task = task,
                                onPause = { onPauseTask(task) },
                                onResume = { onResumeTask(task) },
                                onCancel = { onCancelTask(task) }
                            )
                        }
                    }
                }
            }
            MyFilesTab.ALL -> {
                CompletedList(
                    items = completedAll,
                    onPlay = onPlayItem,
                    onDelete = onDeleteTask,
                    onShare = onShareItem,
                    onOpenWith = onOpenWithItem,
                    onToggleVault = onToggleVault,
                    onGoToHome = onGoToHome
                )
            }
            MyFilesTab.MUSIC -> {
                CompletedList(
                    items = completedMusic,
                    onPlay = onPlayItem,
                    onDelete = onDeleteTask,
                    onShare = onShareItem,
                    onOpenWith = onOpenWithItem,
                    onToggleVault = onToggleVault,
                    onGoToHome = onGoToHome
                )
            }
            MyFilesTab.VIDEOS -> {
                CompletedList(
                    items = completedVideos,
                    onPlay = onPlayItem,
                    onDelete = onDeleteTask,
                    onShare = onShareItem,
                    onOpenWith = onOpenWithItem,
                    onToggleVault = onToggleVault,
                    onGoToHome = onGoToHome
                )
            }
            MyFilesTab.HISTORY -> {
                if (watchHistory.isEmpty()) {
                    EmptyState(
                        title = "Historial vacío",
                        subtitle = "Los videos y música que reproduzcas se guardan automáticamente para continuar donde lo dejaste",
                        actionText = "Explorar contenido",
                        onAction = onGoToHome
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${watchHistory.size} elementos en historial",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Borrar todo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SnaptubeRed,
                                modifier = Modifier.clickable { onClearHistory?.invoke() }
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(watchHistory, key = { it.videoId }) { item ->
                                HistoryItemCard(
                                    history = item,
                                    onClick = { onPlayHistoryItem?.invoke(item) },
                                    onDelete = { onDeleteHistoryItem?.invoke(item.videoId) }
                                )
                            }
                        }
                    }
                }
            }
            MyFilesTab.VAULT -> {
                // Vault opened via callback
            }
        }
    }
}

@Composable
private fun DownloadingTaskCard(
    task: DownloadTaskEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("downloading_task_${task.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = task.thumbnailUrl,
                    contentDescription = task.title,
                    modifier = Modifier
                        .size(60.dp, 44.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SnaptubeYellow.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = task.format,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SnaptubeYellow
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${task.progressPercent}% • ${task.downloadSpeedFormatted}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Destino: ${task.localFilePath}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Pause / Resume button
                IconButton(
                    onClick = {
                        if (task.status == DownloadStatus.DOWNLOADING) onPause() else onResume()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (task.status == DownloadStatus.DOWNLOADING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Pausar o Reanudar",
                        tint = SnaptubeYellow
                    )
                }

                // Cancel button
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated progress bar
            LinearProgressIndicator(
                progress = { task.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SnaptubeYellow,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun CompletedList(
    items: List<DownloadTaskEntity>,
    onPlay: (DownloadTaskEntity) -> Unit,
    onDelete: (DownloadTaskEntity) -> Unit,
    onShare: ((DownloadTaskEntity) -> Unit)? = null,
    onOpenWith: ((DownloadTaskEntity) -> Unit)? = null,
    onToggleVault: (DownloadTaskEntity) -> Unit,
    onGoToHome: () -> Unit
) {
    if (items.isEmpty()) {
        EmptyState(
            title = "No hay archivos descargados",
            subtitle = "Los archivos completados aparecerán aquí para ver o escuchar sin internet",
            actionText = "Descargar videos",
            onAction = onGoToHome
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { item ->
                CompletedItemCard(
                    item = item,
                    onPlay = { onPlay(item) },
                    onDelete = { onDelete(item) },
                    onShare = { onShare?.invoke(item) },
                    onOpenWith = { onOpenWith?.invoke(item) },
                    onToggleVault = { onToggleVault(item) }
                )
            }
        }
    }
}

@Composable
private fun CompletedItemCard(
    item: DownloadTaskEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onShare: (() -> Unit)? = null,
    onOpenWith: (() -> Unit)? = null,
    onToggleVault: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isAudio = item.mediaType == MediaType.AUDIO

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("completed_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with Play overlay icon
            Box(
                modifier = Modifier
                    .size(68.dp, 48.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = item.thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop" },
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAudio) Icons.Default.Audiotrack else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isAudio) MusicPurple.copy(alpha = 0.2f) else VideoBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.format,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAudio) MusicPurple else VideoBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.duration} • ${(item.totalSizeBytes / (1024 * 1024))} MB",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ubicación: ${item.localFilePath}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Options 3-dot dropdown
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Reproducir") },
                        onClick = {
                            menuExpanded = false
                            onPlay()
                        },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Compartir archivo") },
                        onClick = {
                            menuExpanded = false
                            onShare?.invoke()
                        },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Abrir con...") },
                        onClick = {
                            menuExpanded = false
                            onOpenWith?.invoke()
                        },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Mover a Bóveda Segura") },
                        onClick = {
                            menuExpanded = false
                            onToggleVault()
                        },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SnaptubeRed) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SnaptubeYellow.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = SnaptubeYellow,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnaptubeYellow,
                    contentColor = Color.Black
                )
            ) {
                Text(text = actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    history: WatchHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp, 56.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                AsyncImage(
                    model = history.thumbnailUrl,
                    contentDescription = history.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                val progress = if (history.durationSeconds > 0) {
                    (history.lastPositionSeconds.toFloat() / history.durationSeconds.toFloat()).coerceIn(0f, 1f)
                } else 0f
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(SnaptubeRed)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val posText = "${history.lastPositionSeconds / 60}:${String.format("%02d", history.lastPositionSeconds % 60)}"
                val durText = "${history.durationSeconds / 60}:${String.format("%02d", history.durationSeconds % 60)}"
                Text(
                    text = "${history.channel} • $posText / $durText",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar de historial",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

