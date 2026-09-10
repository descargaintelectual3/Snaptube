package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookmarkEntity
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTaskEntity
import com.example.data.model.MediaType
import com.example.data.model.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY timestamp DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' AND isInVault = 0 ORDER BY timestamp DESC")
    fun getCompletedLibrary(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' AND mediaType = :mediaType AND isInVault = 0 ORDER BY timestamp DESC")
    fun getCompletedByMediaType(mediaType: MediaType): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM downloads WHERE isInVault = 1 ORDER BY timestamp DESC")
    fun getVaultItems(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(task: DownloadTaskEntity)

    @Update
    suspend fun update(task: DownloadTaskEntity)

    @Delete
    suspend fun delete(task: DownloadTaskEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE downloads SET isInVault = :inVault WHERE id = :id")
    suspend fun updateVaultStatus(id: String, inVault: Boolean)

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: DownloadStatus)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC LIMIT 30")
    fun getWatchHistory(): Flow<List<com.example.data.model.WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE isCompleted = 0 AND lastPositionSeconds > 5 ORDER BY timestamp DESC LIMIT 10")
    fun getContinueWatching(): Flow<List<com.example.data.model.WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    suspend fun getWatchHistoryItem(videoId: String): com.example.data.model.WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(item: com.example.data.model.WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteWatchHistory(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM saved_playlist_items WHERE playlistName = :name ORDER BY addedTimestamp DESC")
    fun getPlaylistItems(name: String): Flow<List<com.example.data.model.SavedPlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: com.example.data.model.SavedPlaylistItemEntity)

    @Query("DELETE FROM saved_playlist_items WHERE videoId = :videoId AND playlistName = :name")
    suspend fun removePlaylistItem(videoId: String, name: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_playlist_items WHERE videoId = :videoId AND playlistName = :name)")
    suspend fun isItemInPlaylist(videoId: String, name: String): Boolean
}
