package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaType
import com.example.data.repository.MediaCatalog
import com.example.ui.components.DirectUrlDialog
import com.example.ui.components.DownloadFormatDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.MusicPlayerModal
import com.example.ui.components.SnaptubeBottomBar
import com.example.ui.components.SnaptubeTopBar
import com.example.ui.components.VideoPlayerModal
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.CleanerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeScreen
import com.example.ui.screens.MyFilesScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.StatusSaverScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MyFilesTab
import com.example.ui.viewmodel.SnaptubeNavTab
import com.example.ui.viewmodel.SnaptubeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SnaptubeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
            MyApplicationTheme(darkTheme = darkModeEnabled) {
                SnaptubeApp(
                    viewModel = viewModel,
                    onEnterPiP = { enterPipMode() }
                )
            }
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val playback = viewModel.playbackManager.playbackState.value
        if (playback.isPlaying && playback.isVideo && !playback.isAudioOnlyMode) {
            enterPipMode()
        }
    }
}

@Composable
fun SnaptubeApp(
    viewModel: SnaptubeViewModel,
    onEnterPiP: () -> Unit = {}
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val activeDownloadVideo by viewModel.activeDownloadVideo.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val browserUrl by viewModel.browserUrl.collectAsState()
    val detectedMediaOnPage by viewModel.detectedMediaOnPage.collectAsState()
    val myFilesTab by viewModel.myFilesTab.collectAsState()
    val isWaStatusOpen by viewModel.isWaStatusOpen.collectAsState()
    val waStatuses by viewModel.waStatuses.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val isVaultOpen by viewModel.isVaultOpen.collectAsState()
    val isCleanerOpen by viewModel.isCleanerOpen.collectAsState()
    val cleanerScanning by viewModel.cleanerScanning.collectAsState()
    val junkCleanedMb by viewModel.junkCleanedMb.collectAsState()
    val isDirectUrlDialogOpen by viewModel.isDirectUrlDialogOpen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
    val wifiOnly by viewModel.downloadOverWifiOnly.collectAsState()
    val maxTasks by viewModel.maxConcurrentDownloads.collectAsState()

    val allDownloads by viewModel.allDownloads.collectAsState()
    val vaultItems by viewModel.vaultItems.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val playbackState by viewModel.playbackManager.playbackState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToast()
        }
    }

    val activeDownloadingCount = allDownloads.count {
        it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PAUSED
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("snaptube_main_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isSearchOpen && !isWaStatusOpen && !isVaultOpen && !isCleanerOpen) {
                SnaptubeTopBar(
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onSearchSubmit = { viewModel.performSearch(it) },
                    onDirectDownloadUrl = { url ->
                        viewModel.startDirectDownload(url, isAudio = false)
                    },
                    activeDownloadsCount = activeDownloadingCount,
                    onSearchClick = { viewModel.openSearch() },
                    onPasteLinkClick = { viewModel.openDirectUrlDialog() },
                    onDownloadsBadgeClick = {
                        viewModel.selectTab(SnaptubeNavTab.MY_FILES)
                        viewModel.setMyFilesTab(MyFilesTab.DOWNLOADING)
                    }
                )
            }
        },
        bottomBar = {
            if (!isSearchOpen && !isWaStatusOpen && !isVaultOpen && !isCleanerOpen) {
                Column {
                    // Mini player bar when music or video is active
                    MiniPlayerBar(
                        state = playbackState,
                        onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
                        onExpand = { viewModel.playbackManager.openFullScreen() },
                        onClose = { viewModel.playbackManager.closePlayer() }
                    )
                    SnaptubeBottomBar(
                        currentTab = currentTab,
                        activeDownloadsCount = activeDownloadingCount,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                SnaptubeNavTab.HOME -> {
                    HomeScreen(
                        videos = searchResults,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onVideoClick = { video ->
                            viewModel.playbackManager.playMedia(
                                id = video.id,
                                title = video.title,
                                subtitle = video.channel,
                                thumbnailUrl = video.thumbnailUrl,
                                mediaUrl = video.videoUrl,
                                isVideo = true,
                                openFullScreen = true
                            )
                        },
                        onDownloadClick = { video ->
                            viewModel.openDownloadSheet(video)
                        },
                        onQuickSiteClick = { site ->
                            if (site.isWaStatus) {
                                viewModel.openWaStatusSaver()
                            } else {
                                viewModel.navigateBrowserTo(site.url)
                            }
                        },
                        onSearchKeywordClick = { keyword ->
                            viewModel.openSearch()
                            viewModel.performSearch(keyword)
                        }
                    )
                }

                SnaptubeNavTab.BROWSER -> {
                    BrowserScreen(
                        currentUrl = browserUrl,
                        detectedMedia = detectedMediaOnPage,
                        onNavigate = { newUrl ->
                            viewModel.navigateBrowserTo(newUrl)
                        },
                        onDownloadDetectedMedia = { video ->
                            viewModel.openDownloadSheet(video)
                        }
                    )
                }

                SnaptubeNavTab.MY_FILES -> {
                    MyFilesScreen(
                        currentTab = myFilesTab,
                        onTabSelected = { viewModel.setMyFilesTab(it) },
                        allDownloads = allDownloads,
                        onPlayItem = { task ->
                            viewModel.playbackManager.playMedia(
                                id = task.id,
                                title = task.title,
                                subtitle = task.channel,
                                thumbnailUrl = task.thumbnailUrl,
                                mediaUrl = task.sourceUrl,
                                isVideo = task.mediaType == MediaType.VIDEO,
                                openFullScreen = true
                            )
                        },
                        onPauseTask = { task -> viewModel.downloadEngine.pauseDownload(task) },
                        onResumeTask = { task -> viewModel.downloadEngine.resumeDownload(task) },
                        onCancelTask = { task -> viewModel.downloadEngine.cancelDownload(task) },
                        onDeleteTask = { task -> viewModel.deleteDownload(task) },
                        onToggleVault = { task -> viewModel.toggleVaultItem(task) },
                        onOpenVault = { viewModel.openVault() },
                        onGoToHome = { viewModel.selectTab(SnaptubeNavTab.HOME) }
                    )
                }

                SnaptubeNavTab.ME -> {
                    MeScreen(
                        onOpenWaStatus = { viewModel.openWaStatusSaver() },
                        onOpenCleaner = { viewModel.openCleaner() },
                        onOpenVault = { viewModel.openVault() },
                        darkModeEnabled = darkModeEnabled,
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        wifiOnly = wifiOnly,
                        maxTasks = maxTasks
                    )
                }
            }

            // Search Overlay View
            if (isSearchOpen) {
                SearchScreen(
                    query = searchQuery,
                    searchResults = searchResults,
                    recentSearches = searchHistory,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onSearchSubmit = { viewModel.performSearch(it) },
                    onClearHistory = { viewModel.clearSearchHistory() },
                    onDeleteHistoryItem = { viewModel.deleteSearchHistoryItem(it) },
                    onVideoClick = { video ->
                        viewModel.playbackManager.playMedia(
                            id = video.id,
                            title = video.title,
                            subtitle = video.channel,
                            thumbnailUrl = video.thumbnailUrl,
                            mediaUrl = video.videoUrl,
                            isVideo = true,
                            openFullScreen = true
                        )
                    },
                    onDownloadClick = { video ->
                        viewModel.openDownloadSheet(video)
                    },
                    onClose = { viewModel.closeSearch() }
                )
            }

            // WhatsApp Status Saver Overlay
            if (isWaStatusOpen) {
                StatusSaverScreen(
                    statuses = waStatuses,
                    onSaveStatus = { viewModel.saveWaStatus(it) },
                    onScanDevice = { viewModel.scanWhatsAppMediaFolders(context) },
                    onClose = { viewModel.closeWaStatusSaver() }
                )
            }

            // Private Safe Vault Overlay
            if (isVaultOpen) {
                VaultScreen(
                    isUnlocked = isVaultUnlocked,
                    vaultItems = vaultItems,
                    onUnlock = { pin -> viewModel.unlockVault(pin) },
                    onRestoreItem = { item -> viewModel.toggleVaultItem(item) },
                    onPlayItem = { item ->
                        viewModel.playbackManager.playMedia(
                            id = item.id,
                            title = item.title,
                            subtitle = item.channel,
                            thumbnailUrl = item.thumbnailUrl,
                            mediaUrl = item.sourceUrl,
                            isVideo = item.mediaType == MediaType.VIDEO,
                            openFullScreen = true
                        )
                    },
                    onClose = { viewModel.closeVault() }
                )
            }

            // Storage Cleaner Overlay
            if (isCleanerOpen) {
                CleanerScreen(
                    isScanning = cleanerScanning,
                    junkMb = junkCleanedMb,
                    onClean = { viewModel.executeCleanJunk() },
                    onClose = { viewModel.closeCleaner() }
                )
            }
        }
    }

    // Snaptube Resolution & Quality Bottom Sheet
    activeDownloadVideo?.let { video ->
        DownloadFormatDialog(
            video = video,
            onDismiss = { viewModel.closeDownloadSheet() },
            onConfirmDownload = { quality ->
                viewModel.confirmDownload(quality)
            }
        )
    }

    // Direct URL Downloader Dialog
    DirectUrlDialog(
        isOpen = isDirectUrlDialogOpen,
        onDismiss = { viewModel.closeDirectUrlDialog() },
        onConfirmDownload = { url, isAudio ->
            viewModel.startDirectDownload(url, isAudio)
        }
    )

    // Full Screen Video Player (YouTube Premium Experience)
    VideoPlayerModal(
        state = playbackState,
        onClose = { viewModel.playbackManager.closeFullScreen() },
        onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
        onSeek = { fraction -> viewModel.playbackManager.seekToFraction(fraction) },
        onForward10 = { viewModel.playbackManager.forward10() },
        onRewind10 = { viewModel.playbackManager.rewind10() },
        onSetSpeed = { speed -> viewModel.playbackManager.setSpeed(speed) },
        onDownloadClick = { video -> viewModel.openDownloadSheet(video) },
        onToggleAudioOnly = { viewModel.playbackManager.toggleAudioOnlyMode() },
        onEnterPiP = { onEnterPiP() },
        onPlayNext = { viewModel.playbackManager.playNextInQueue() },
        onToggleAutoplay = { viewModel.playbackManager.toggleAutoplay() },
        onSetSleepTimer = { minutes -> viewModel.playbackManager.setSleepTimer(minutes) },
        onPlayItemFromQueue = { video ->
            viewModel.playbackManager.playMedia(
                id = video.id,
                title = video.title,
                subtitle = video.channel,
                thumbnailUrl = video.thumbnailUrl,
                mediaUrl = video.videoUrl,
                isVideo = true,
                openFullScreen = true
            )
        }
    )

    // Full Screen Hi-Fi Music Turntable Player
    MusicPlayerModal(
        state = playbackState,
        onClose = { viewModel.playbackManager.closeFullScreen() },
        onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
        onSeek = { fraction -> viewModel.playbackManager.seekToFraction(fraction) },
        onToggleShuffle = { viewModel.playbackManager.toggleShuffle() },
        onToggleRepeat = { viewModel.playbackManager.toggleRepeat() },
        onForward10 = { viewModel.playbackManager.forward10() },
        onRewind10 = { viewModel.playbackManager.rewind10() }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Snaptube $name", modifier = modifier)
}
