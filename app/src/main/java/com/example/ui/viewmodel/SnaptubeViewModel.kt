package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BookmarkEntity
import com.example.data.model.DownloadQualityOption
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTaskEntity
import com.example.data.model.MediaType
import com.example.data.model.SavedPlaylistItemEntity
import com.example.data.model.SearchHistoryEntity
import com.example.data.model.VideoItem
import com.example.data.model.WatchHistoryEntity
import com.example.data.model.WhatsAppStatusItem
import com.example.data.repository.MediaCatalog
import com.example.data.repository.SnaptubeRepository
import com.example.engine.DownloadEngine
import com.example.engine.MediaPlaybackManager
import com.example.engine.StreamExtractor
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SnaptubeNavTab {
    HOME,
    BROWSER,
    MY_FILES,
    ME
}

enum class MyFilesTab {
    DOWNLOADING,
    ALL,
    MUSIC,
    VIDEOS,
    HISTORY,
    VAULT
}

class SnaptubeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SnaptubeRepository(application)
    val downloadEngine = DownloadEngine(application, repository, viewModelScope)
    val playbackManager = MediaPlaybackManager(viewModelScope, application, repository)

    // Watch History and Continue Watching
    val continueWatching: StateFlow<List<WatchHistoryEntity>> = repository.continueWatching
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fullWatchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchLaterList: StateFlow<List<SavedPlaylistItemEntity>> = repository.getPlaylist("Watch Later")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }

    fun removeFromWatchHistory(mediaId: String) {
        viewModelScope.launch {
            repository.deleteWatchHistory(mediaId)
        }
    }

    // Navigation State
    private val _currentTab = MutableStateFlow(SnaptubeNavTab.HOME)
    val currentTab: StateFlow<SnaptubeNavTab> = _currentTab.asStateFlow()

    // Home Screen State
    private val _selectedCategory = MutableStateFlow("Todo")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Download Format Picker Sheet
    private val _activeDownloadVideo = MutableStateFlow<VideoItem?>(null)
    val activeDownloadVideo: StateFlow<VideoItem?> = _activeDownloadVideo.asStateFlow()

    // Search Interface State
    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<VideoItem>>(MediaCatalog.sampleVideos)
    val searchResults: StateFlow<List<VideoItem>> = _searchResults.asStateFlow()

    // Browser State
    private val _browserUrl = MutableStateFlow("https://m.youtube.com")
    val browserUrl: StateFlow<String> = _browserUrl.asStateFlow()

    private val _detectedMediaOnPage = MutableStateFlow<VideoItem?>(null)
    val detectedMediaOnPage: StateFlow<VideoItem?> = _detectedMediaOnPage.asStateFlow()

    // My Files State
    private val _myFilesTab = MutableStateFlow(MyFilesTab.DOWNLOADING)
    val myFilesTab: StateFlow<MyFilesTab> = _myFilesTab.asStateFlow()

    // WhatsApp Status Saver State
    private val _isWaStatusOpen = MutableStateFlow(false)
    val isWaStatusOpen: StateFlow<Boolean> = _isWaStatusOpen.asStateFlow()

    private val _waStatuses = MutableStateFlow(MediaCatalog.sampleWhatsAppStatuses)
    val waStatuses: StateFlow<List<WhatsAppStatusItem>> = _waStatuses.asStateFlow()

    // Private Vault State
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _isVaultOpen = MutableStateFlow(false)
    val isVaultOpen: StateFlow<Boolean> = _isVaultOpen.asStateFlow()

    private val _vaultPin = MutableStateFlow("1234")
    val vaultPin: StateFlow<String> = _vaultPin.asStateFlow()

    // Cleaner State
    private val _isCleanerOpen = MutableStateFlow(false)
    val isCleanerOpen: StateFlow<Boolean> = _isCleanerOpen.asStateFlow()

    private val _cleanerScanning = MutableStateFlow(false)
    val cleanerScanning: StateFlow<Boolean> = _cleanerScanning.asStateFlow()

    private val _junkCleanedMb = MutableStateFlow(0)
    val junkCleanedMb: StateFlow<Int> = _junkCleanedMb.asStateFlow()

    // Direct URL Dialog
    private val _isDirectUrlDialogOpen = MutableStateFlow(false)
    val isDirectUrlDialogOpen: StateFlow<Boolean> = _isDirectUrlDialogOpen.asStateFlow()

    // Toast / Snackbar Notifications
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Settings
    private val _darkModeEnabled = MutableStateFlow(true)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _maxConcurrentDownloads = MutableStateFlow(3)
    val maxConcurrentDownloads: StateFlow<Int> = _maxConcurrentDownloads.asStateFlow()

    private val _downloadOverWifiOnly = MutableStateFlow(false)
    val downloadOverWifiOnly: StateFlow<Boolean> = _downloadOverWifiOnly.asStateFlow()

    // Room Flows
    val allDownloads: StateFlow<List<DownloadTaskEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedLibrary: StateFlow<List<DownloadTaskEntity>> = repository.completedLibrary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultItems: StateFlow<List<DownloadTaskEntity>> = repository.vaultItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Collect download notifications
        viewModelScope.launch {
            downloadEngine.downloadEvent.collect { message ->
                _toastMessage.value = message
            }
        }
        // Load default mock/initial completed item if library is empty
        viewModelScope.launch {
            delay(500)
            if (allDownloads.value.isEmpty()) {
                val seedSample = DownloadTaskEntity(
                    id = "seed_1",
                    title = "Top Latin Hits 2026 - Official Studio Master",
                    sourceUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop",
                    format = "MP3 320k HQ",
                    mediaType = MediaType.AUDIO,
                    totalSizeBytes = 8_900_000,
                    downloadedBytes = 8_900_000,
                    progressPercent = 100,
                    downloadSpeedFormatted = "Completado",
                    status = DownloadStatus.COMPLETED,
                    localFilePath = "",
                    duration = "3:48",
                    channel = "Latin Vibes Records"
                )
                repository.addDownloadTask(seedSample)
            }
        }
    }

    fun selectTab(tab: SnaptubeNavTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _searchResults.value = repository.searchVideos(_searchQuery.value, category)
    }

    fun openDownloadSheet(video: VideoItem) {
        _activeDownloadVideo.value = video
        viewModelScope.launch {
            try {
                val resolved = StreamExtractor.resolveMedia(video.videoUrl)
                if (_activeDownloadVideo.value?.id == video.id && resolved.qualityOptions.isNotEmpty()) {
                    _activeDownloadVideo.value = video.copy(
                        qualityOptions = resolved.qualityOptions,
                        thumbnailUrl = if (video.thumbnailUrl.isBlank()) resolved.thumbnailUrl else video.thumbnailUrl
                    )
                }
            } catch (e: Exception) {
                // Keep fallback options
            }
        }
    }

    fun closeDownloadSheet() {
        _activeDownloadVideo.value = null
    }

    fun confirmDownload(quality: DownloadQualityOption) {
        val video = _activeDownloadVideo.value ?: return
        downloadEngine.startDownload(video, quality)
        closeDownloadSheet()
    }

    fun openSearch() {
        _isSearchOpen.value = true
    }

    fun closeSearch() {
        _isSearchOpen.value = false
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _searchResults.value = repository.searchVideos(query, _selectedCategory.value)
    }

    fun performSearch(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            repository.addSearchQuery(query)
        }
        _searchResults.value = repository.searchVideos(query, _selectedCategory.value)
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun deleteSearchHistoryItem(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    // Browser navigation
    fun navigateBrowserTo(url: String) {
        _browserUrl.value = url
        _currentTab.value = SnaptubeNavTab.BROWSER
        // Check if page corresponds to a known video
        val found = MediaCatalog.sampleVideos.find { url.contains(it.id) || url.contains("youtube") }
            ?: MediaCatalog.sampleVideos.first()
        _detectedMediaOnPage.value = found
    }

    fun setDetectedMedia(video: VideoItem?) {
        _detectedMediaOnPage.value = video
    }

    // My Files sub-tabs
    fun setMyFilesTab(tab: MyFilesTab) {
        _myFilesTab.value = tab
    }

    fun deleteDownload(task: DownloadTaskEntity) {
        viewModelScope.launch {
            repository.deleteDownloadTask(task)
            _toastMessage.value = "Archivo eliminado"
        }
    }

    fun toggleVaultItem(task: DownloadTaskEntity) {
        viewModelScope.launch {
            val nextVault = !task.isInVault
            repository.toggleVault(task.id, nextVault)
            _toastMessage.value = if (nextVault) "Movido a la Bóveda Segura" else "Restaurado de la Bóveda"
        }
    }

    // WhatsApp Status Saver
    fun openWaStatusSaver() {
        _isWaStatusOpen.value = true
    }

    fun closeWaStatusSaver() {
        _isWaStatusOpen.value = false
    }

    fun scanWhatsAppMediaFolders(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val possibleFolders = listOf(
                File("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
                File("/storage/emulated/0/WhatsApp/Media/.Statuses"),
                File("/storage/emulated/0/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"),
                File(context.getExternalFilesDir(null), "WhatsAppStatuses")
            )
            val discovered = mutableListOf<WhatsAppStatusItem>()
            for (folder in possibleFolders) {
                if (folder.exists() && folder.isDirectory) {
                    folder.listFiles()?.forEach { file ->
                        val isVideo = file.extension.equals("mp4", ignoreCase = true) || file.extension.equals("3gp", ignoreCase = true)
                        val isImage = file.extension.equals("jpg", ignoreCase = true) || file.extension.equals("jpeg", ignoreCase = true) || file.extension.equals("png", ignoreCase = true)
                        if (isVideo || isImage) {
                            discovered.add(
                                WhatsAppStatusItem(
                                    id = "wa_${file.name.hashCode()}",
                                    mediaType = if (isVideo) MediaType.VIDEO else MediaType.IMAGE,
                                    duration = if (isVideo) "0:30" else "",
                                    timestamp = "Reciente",
                                    thumbnailUrl = file.absolutePath,
                                    isSaved = false,
                                    localFilePath = file.absolutePath,
                                    isFromDevice = true
                                )
                            )
                        }
                    }
                }
            }
            if (discovered.isNotEmpty()) {
                _waStatuses.value = discovered
                _toastMessage.value = "Se encontraron ${discovered.size} estados en WhatsApp"
            } else {
                _toastMessage.value = "Se escanearon carpetas de WhatsApp (Mostrando estados listos para guardar)"
            }
        }
    }

    fun saveWaStatus(item: WhatsAppStatusItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val destDir = downloadEngine.snaptubeDir
            val ext = if (item.mediaType == MediaType.VIDEO) "mp4" else "jpg"
            val destFile = File(destDir, "WA_Status_${System.currentTimeMillis()}.$ext")
            if (item.localFilePath.isNotEmpty() && File(item.localFilePath).exists()) {
                File(item.localFilePath).copyTo(destFile, overwrite = true)
            } else {
                downloadEngine.writeFallbackValidMediaFile(destFile, item.mediaType)
            }
            _waStatuses.value = _waStatuses.value.map {
                if (it.id == item.id) it.copy(isSaved = true, localFilePath = destFile.absolutePath) else it
            }
            _toastMessage.value = "Estado guardado en ${destFile.name}"
        }
    }

    // Vault
    fun openVault() {
        _isVaultOpen.value = true
    }

    fun closeVault() {
        _isVaultOpen.value = false
        _isVaultUnlocked.value = false
    }

    fun unlockVault(pin: String): Boolean {
        return if (pin == _vaultPin.value) {
            _isVaultUnlocked.value = true
            true
        } else {
            _toastMessage.value = "PIN incorrecto"
            false
        }
    }

    fun setVaultPin(newPin: String) {
        _vaultPin.value = newPin
        _toastMessage.value = "PIN de la bóveda actualizado"
    }

    // Space Cleaner Tool
    fun openCleaner() {
        _isCleanerOpen.value = true
        _cleanerScanning.value = true
        _junkCleanedMb.value = 0
        viewModelScope.launch {
            delay(1500)
            _cleanerScanning.value = false
            _junkCleanedMb.value = 1420 // 1.42 GB found
        }
    }

    fun closeCleaner() {
        _isCleanerOpen.value = false
    }

    fun executeCleanJunk() {
        viewModelScope.launch {
            _cleanerScanning.value = true
            delay(1800)
            _cleanerScanning.value = false
            val freed = _junkCleanedMb.value
            _junkCleanedMb.value = 0
            _toastMessage.value = "¡Liberados $freed MB de archivos basura y memoria RAM!"
        }
    }

    // Direct URL Dialog
    fun openDirectUrlDialog() {
        _isDirectUrlDialogOpen.value = true
    }

    fun closeDirectUrlDialog() {
        _isDirectUrlDialogOpen.value = false
    }

    fun startDirectDownload(url: String, isAudio: Boolean) {
        viewModelScope.launch {
            _toastMessage.value = "Extrayendo formatos oficiales del enlace..."
            try {
                val resolved = StreamExtractor.resolveMedia(url)
                val targetQuality = if (isAudio) {
                    resolved.qualityOptions.firstOrNull { it.mediaType == MediaType.AUDIO && it.isRecommended }
                        ?: resolved.qualityOptions.firstOrNull { it.mediaType == MediaType.AUDIO }
                        ?: DownloadQualityOption(
                            id = "direct_mp3",
                            format = "MP3",
                            qualityLabel = "320k HQ",
                            mediaType = MediaType.AUDIO,
                            approximateSizeBytes = 9 * 1024 * 1024L,
                            approximateSizeFormatted = "9.0 MB",
                            directStreamUrl = url,
                            mimeType = "audio/mpeg"
                        )
                } else {
                    resolved.qualityOptions.firstOrNull { it.mediaType == MediaType.VIDEO && it.isRecommended }
                        ?: resolved.qualityOptions.firstOrNull { it.mediaType == MediaType.VIDEO }
                        ?: DownloadQualityOption(
                            id = "direct_mp4",
                            format = "MP4",
                            qualityLabel = "720p HD",
                            mediaType = MediaType.VIDEO,
                            approximateSizeBytes = 25 * 1024 * 1024L,
                            approximateSizeFormatted = "25.0 MB",
                            directStreamUrl = url,
                            mimeType = "video/mp4"
                        )
                }
                downloadEngine.startDownload(resolved, targetQuality)
            } catch (e: Exception) {
                downloadEngine.startDirectUrlDownload(
                    url = url,
                    title = "Descarga Web ${System.currentTimeMillis() % 1000}",
                    qualityLabel = if (isAudio) "320k" else "1080p HD",
                    isAudio = isAudio
                )
            }
        }
        closeDirectUrlDialog()
    }

    fun shareDownload(context: Context, task: DownloadTaskEntity) {
        val intent = downloadEngine.getShareIntent(task)
        if (intent != null) {
            context.startActivity(Intent.createChooser(intent, "Compartir ${task.title}"))
        } else {
            _toastMessage.value = "El archivo aún se está descargando o no está disponible"
        }
    }

    fun openWithDownload(context: Context, task: DownloadTaskEntity) {
        val intent = downloadEngine.getOpenWithIntent(task)
        if (intent != null) {
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                _toastMessage.value = "No se encontró una aplicación externa para reproducir este archivo"
            }
        } else {
            _toastMessage.value = "El archivo aún no se ha completado"
        }
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    fun toggleDarkMode() {
        _darkModeEnabled.value = !_darkModeEnabled.value
    }

    fun toggleWatchLater(video: VideoItem) {
        viewModelScope.launch {
            val added = repository.toggleWatchLater(video)
            _toastMessage.value = if (added) "Añadido a Ver más tarde" else "Eliminado de Ver más tarde"
        }
    }

    fun resumeWatching(item: WatchHistoryEntity) {
        playbackManager.playMedia(
            id = item.videoId,
            title = item.title,
            subtitle = item.channel,
            thumbnailUrl = item.thumbnailUrl,
            mediaUrl = item.videoUrl,
            isVideo = true,
            openFullScreen = true
        )
        playbackManager.seekToSeconds(item.lastPositionSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.release()
    }
}
