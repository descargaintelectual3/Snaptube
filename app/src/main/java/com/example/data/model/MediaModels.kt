package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Format types supported by Snaptube
 */
enum class MediaType {
    VIDEO,
    AUDIO,
    IMAGE
}

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

data class DownloadQualityOption(
    val id: String,
    val format: String, // "MP3", "M4A", "MP4"
    val qualityLabel: String, // "128k", "320k HQ", "720p HD", "1080p FHD", "4K Ultra"
    val mediaType: MediaType,
    val approximateSizeBytes: Long,
    val approximateSizeFormatted: String,
    val isRecommended: Boolean = false,
    val directStreamUrl: String = "",
    val mimeType: String = ""
)

data class VideoItem(
    val id: String,
    val title: String,
    val channel: String,
    val duration: String,
    val viewCount: String,
    val publishedTime: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val category: String = "Tendencias",
    val qualityOptions: List<DownloadQualityOption> = emptyList()
)

@Entity(tableName = "downloads")
data class DownloadTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val sourceUrl: String,
    val thumbnailUrl: String,
    val format: String, // e.g. "MP3 320k" or "MP4 1080p"
    val mediaType: MediaType,
    val totalSizeBytes: Long,
    val downloadedBytes: Long = 0,
    val progressPercent: Int = 0,
    val downloadSpeedFormatted: String = "0 KB/s",
    val status: DownloadStatus = DownloadStatus.DOWNLOADING,
    val localFilePath: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val duration: String = "3:45",
    val channel: String = "Snaptube Media",
    val isInVault: Boolean = false
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val url: String,
    val title: String,
    val iconName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channel: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val lastPositionSeconds: Int,
    val durationSeconds: Int,
    val durationFormatted: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Entity(tableName = "saved_playlist_items")
data class SavedPlaylistItemEntity(
    @PrimaryKey val videoId: String,
    val playlistName: String, // "Watch Later" or "Favorites"
    val title: String,
    val channel: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)

data class VideoChapter(
    val title: String,
    val startTimeSeconds: Int,
    val formattedTime: String
)

data class WhatsAppStatusItem(
    val id: String,
    val mediaType: MediaType,
    val duration: String = "",
    val timestamp: String = "Hace 20 min",
    val thumbnailUrl: String,
    val isSaved: Boolean = false,
    val localFilePath: String = "",
    val isFromDevice: Boolean = false
)
