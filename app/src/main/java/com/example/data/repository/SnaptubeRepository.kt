package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.BookmarkEntity
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTaskEntity
import com.example.data.model.MediaType
import com.example.data.model.SearchHistoryEntity
import com.example.data.model.VideoItem
import kotlinx.coroutines.flow.Flow
import java.io.File

class SnaptubeRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val downloadDao = db.downloadDao()
    private val searchHistoryDao = db.searchHistoryDao()
    private val bookmarkDao = db.bookmarkDao()
    private val watchHistoryDao = db.watchHistoryDao()
    private val playlistDao = db.playlistDao()

    val allDownloads: Flow<List<DownloadTaskEntity>> = downloadDao.getAllDownloads()
    val completedLibrary: Flow<List<DownloadTaskEntity>> = downloadDao.getCompletedLibrary()
    val vaultItems: Flow<List<DownloadTaskEntity>> = downloadDao.getVaultItems()
    val searchHistory: Flow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentSearches()
    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val continueWatching: Flow<List<com.example.data.model.WatchHistoryEntity>> = watchHistoryDao.getContinueWatching()
    val watchHistory: Flow<List<com.example.data.model.WatchHistoryEntity>> = watchHistoryDao.getWatchHistory()

    fun getCompletedByMediaType(mediaType: MediaType): Flow<List<DownloadTaskEntity>> {
        return downloadDao.getCompletedByMediaType(mediaType)
    }

    suspend fun addDownloadTask(task: DownloadTaskEntity) {
        downloadDao.insertOrUpdate(task)
    }

    suspend fun updateDownloadTask(task: DownloadTaskEntity) {
        downloadDao.update(task)
    }

    suspend fun deleteDownloadTask(task: DownloadTaskEntity) {
        // Also delete file if exists
        try {
            if (task.localFilePath.isNotEmpty()) {
                val file = File(task.localFilePath)
                if (file.exists()) file.delete()
            }
        } catch (_: Exception) {}
        downloadDao.delete(task)
    }

    suspend fun toggleVault(id: String, inVault: Boolean) {
        downloadDao.updateVaultStatus(id, inVault)
    }

    suspend fun updateTaskStatus(id: String, status: DownloadStatus) {
        downloadDao.updateStatus(id, status)
    }

    suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = query.trim()))
        }
    }

    suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteByQuery(query)
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearHistory()
    }

    suspend fun addBookmark(url: String, title: String, iconName: String) {
        bookmarkDao.insertBookmark(BookmarkEntity(url = url, title = title, iconName = iconName))
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    suspend fun recordWatchProgress(
        videoId: String,
        title: String,
        channel: String,
        thumbnailUrl: String,
        videoUrl: String,
        positionSeconds: Int,
        durationSeconds: Int
    ) {
        val completed = durationSeconds > 0 && positionSeconds >= (durationSeconds - 5)
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        val durStr = String.format("%02d:%02d", mins, secs)
        watchHistoryDao.upsertWatchHistory(
            com.example.data.model.WatchHistoryEntity(
                videoId = videoId,
                title = title,
                channel = channel,
                thumbnailUrl = thumbnailUrl,
                videoUrl = videoUrl,
                lastPositionSeconds = positionSeconds,
                durationSeconds = durationSeconds,
                durationFormatted = durStr,
                timestamp = System.currentTimeMillis(),
                isCompleted = completed
            )
        )
    }

    suspend fun clearWatchHistory() {
        watchHistoryDao.clearWatchHistory()
    }

    suspend fun deleteWatchHistory(videoId: String) {
        watchHistoryDao.deleteWatchHistory(videoId)
    }

    fun getPlaylist(name: String): Flow<List<com.example.data.model.SavedPlaylistItemEntity>> {
        return playlistDao.getPlaylistItems(name)
    }

    suspend fun toggleWatchLater(video: VideoItem): Boolean {
        val exists = playlistDao.isItemInPlaylist(video.id, "Watch Later")
        if (exists) {
            playlistDao.removePlaylistItem(video.id, "Watch Later")
            return false
        } else {
            playlistDao.insertPlaylistItem(
                com.example.data.model.SavedPlaylistItemEntity(
                    videoId = video.id,
                    playlistName = "Watch Later",
                    title = video.title,
                    channel = video.channel,
                    thumbnailUrl = video.thumbnailUrl,
                    videoUrl = video.videoUrl,
                    duration = video.duration
                )
            )
            return true
        }
    }

    fun searchVideos(query: String, categoryFilter: String = "Todo"): List<VideoItem> {
        val trimmed = query.trim().lowercase()
        return MediaCatalog.sampleVideos.filter { video ->
            val matchesCategory = (categoryFilter == "Todo" || video.category.equals(categoryFilter, ignoreCase = true))
            val matchesQuery = if (trimmed.isEmpty()) true else {
                video.title.lowercase().contains(trimmed) ||
                video.channel.lowercase().contains(trimmed) ||
                video.category.lowercase().contains(trimmed)
            }
            matchesCategory && matchesQuery
        }
    }
}
