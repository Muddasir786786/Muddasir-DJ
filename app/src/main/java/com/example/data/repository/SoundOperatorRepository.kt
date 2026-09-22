package com.example.data.repository

import com.example.data.dao.PlayHistoryWithSong
import com.example.data.dao.QueueItemWithSong
import com.example.data.database.AppDatabase
import com.example.data.entity.CategoryEntity
import com.example.data.entity.FavoriteEntity
import com.example.data.entity.MusicPackEntity
import com.example.data.entity.PlayHistoryEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.PlaylistSongEntity
import com.example.data.entity.QueueItemEntity
import com.example.data.entity.SettingEntity
import com.example.data.entity.SongCategoryEntity
import com.example.data.entity.SongEntity
import kotlinx.coroutines.flow.Flow

class SoundOperatorRepository(private val database: AppDatabase) {

    // Songs
    val allSongs: Flow<List<SongEntity>> = database.songDao().getAllSongs()

    suspend fun getSongById(id: Long): SongEntity? = database.songDao().getSongById(id)

    fun getSongFlowById(id: Long): Flow<SongEntity?> = database.songDao().getSongFlowById(id)

    fun searchSongs(query: String): Flow<List<SongEntity>> = database.songDao().searchSongs(query)

    fun getSongsByCategoryId(categoryId: Long): Flow<List<SongEntity>> =
        database.songDao().getSongsByCategoryId(categoryId)

    fun getCategoriesForSong(songId: Long): Flow<List<CategoryEntity>> =
        database.songDao().getCategoriesForSong(songId)

    suspend fun insertSong(song: SongEntity, categoryIds: List<Long> = emptyList()): Long {
        val songId = database.songDao().insertSong(song)
        if (categoryIds.isNotEmpty()) {
            val mappings = categoryIds.map { SongCategoryEntity(songId, it) }
            database.songDao().insertSongCategories(mappings)
        }
        return songId
    }

    suspend fun updateSong(song: SongEntity) = database.songDao().updateSong(song)

    suspend fun deleteSong(song: SongEntity) = database.songDao().deleteSong(song)

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = database.categoryDao().getAllCategories()

    // Playlists
    val allPlaylists: Flow<List<PlaylistEntity>> = database.playlistDao().getAllPlaylists()

    suspend fun getPlaylistById(id: Long): PlaylistEntity? = database.playlistDao().getPlaylistById(id)

    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>> =
        database.playlistDao().getSongsInPlaylist(playlistId)

    suspend fun createPlaylist(name: String, description: String, colorHex: String = "#00E5FF"): Long {
        return database.playlistDao().insertPlaylist(
            PlaylistEntity(name = name, description = description, coverColorHex = colorHex)
        )
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) = database.playlistDao().deletePlaylist(playlist)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, orderIndex: Int = 0) {
        database.playlistDao().addSongToPlaylist(PlaylistSongEntity(playlistId, songId, orderIndex))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        database.playlistDao().removeSongFromPlaylist(playlistId, songId)
    }

    // Favorites
    val favoriteSongs: Flow<List<SongEntity>> = database.favoriteDao().getFavoriteSongs()
    val favoriteSongIds: Flow<List<Long>> = database.favoriteDao().getAllFavoriteSongIds()

    fun isFavorite(songId: Long): Flow<Boolean> = database.favoriteDao().isFavorite(songId)

    suspend fun toggleFavorite(songId: Long) {
        val isFav = database.favoriteDao().isFavoriteDirect(songId)
        if (isFav) {
            database.favoriteDao().deleteFavorite(songId)
        } else {
            database.favoriteDao().insertFavorite(FavoriteEntity(songId))
        }
    }

    // Play History
    val recentHistory: Flow<List<PlayHistoryWithSong>> = database.playHistoryDao().getRecentHistory()
    val recentlyPlayedSongs: Flow<List<SongEntity>> = database.playHistoryDao().getRecentlyPlayedSongs()

    suspend fun recordPlayHistory(songId: Long, durationPlayedMs: Long = 0, eventName: String = "Live Stage") {
        database.playHistoryDao().insertHistory(
            PlayHistoryEntity(
                songId = songId,
                durationPlayedMs = durationPlayedMs,
                eventName = eventName
            )
        )
    }

    suspend fun clearHistory() = database.playHistoryDao().clearHistory()

    // Queue
    val queueWithSongs: Flow<List<QueueItemWithSong>> = database.queueDao().getQueueWithSongs()

    suspend fun addToQueue(songId: Long) {
        val maxIndex = database.queueDao().getMaxOrderIndex() ?: -1
        database.queueDao().insertQueueItem(
            QueueItemEntity(songId = songId, orderIndex = maxIndex + 1)
        )
    }

    suspend fun playNextInQueue(songId: Long) {
        val currentQueue = database.queueDao().getMaxOrderIndex() ?: 0
        // Insert with top priority
        database.queueDao().insertQueueItem(
            QueueItemEntity(songId = songId, orderIndex = 0)
        )
    }

    suspend fun removeFromQueue(queueId: Long) = database.queueDao().deleteQueueItem(queueId)

    suspend fun clearQueue() = database.queueDao().clearQueue()

    suspend fun replaceQueue(songIds: List<Long>) = database.queueDao().replaceQueue(songIds)

    // Music Packs
    val allMusicPacks: Flow<List<MusicPackEntity>> = database.musicPackDao().getAllMusicPacks()

    suspend fun togglePackDownload(pack: MusicPackEntity) {
        database.musicPackDao().updateMusicPack(pack.copy(isDownloaded = !pack.isDownloaded))
    }

    // Settings
    fun getSettingFlow(key: String): Flow<String?> = database.settingDao().getSettingFlow(key)

    suspend fun getSetting(key: String): String? = database.settingDao().getSetting(key)

    suspend fun setSetting(key: String, value: String) {
        database.settingDao().setSetting(SettingEntity(key, value))
    }

    // YouTube Metadata (Metadata only, no audio/video files cached)
    val youTubeHistory: Flow<List<com.example.data.entity.YouTubeHistoryEntity>> =
        database.youTubeMetadataDao().getRecentHistory()

    val youTubeRecentQueries: Flow<List<com.example.data.entity.YouTubeQueryHistoryEntity>> =
        database.youTubeMetadataDao().getRecentQueries()

    suspend fun recordYouTubeView(
        videoId: String,
        title: String,
        channelTitle: String,
        thumbnailUrl: String,
        durationText: String
    ) {
        database.youTubeMetadataDao().insertHistory(
            com.example.data.entity.YouTubeHistoryEntity(
                videoId = videoId,
                title = title,
                channelTitle = channelTitle,
                thumbnailUrl = thumbnailUrl,
                durationText = durationText,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordYouTubeQuery(query: String) {
        val clean = query.trim().replace("\\s+".toRegex(), " ")
        if (clean.isNotBlank()) {
            database.youTubeMetadataDao().insertQuery(
                com.example.data.entity.YouTubeQueryHistoryEntity(
                    query = clean,
                    timestamp = System.currentTimeMillis()
                )
            )
            database.youTubeMetadataDao().trimQueries(20)
        }
    }

    suspend fun deleteYouTubeQuery(query: String) = database.youTubeMetadataDao().deleteQuery(query)

    suspend fun clearYouTubeHistory() = database.youTubeMetadataDao().clearHistory()

    suspend fun clearYouTubeQueries() = database.youTubeMetadataDao().clearQueries()
}
