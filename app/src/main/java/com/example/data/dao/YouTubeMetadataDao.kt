package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.YouTubeHistoryEntity
import com.example.data.entity.YouTubeQueryHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface YouTubeMetadataDao {

    @Query("SELECT * FROM youtube_history ORDER BY viewedAt DESC LIMIT 30")
    fun getRecentHistory(): Flow<List<YouTubeHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: YouTubeHistoryEntity)

    @Query("DELETE FROM youtube_history WHERE videoId = :videoId")
    suspend fun deleteHistory(videoId: String)

    @Query("DELETE FROM youtube_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM youtube_recent_queries ORDER BY timestamp DESC LIMIT 20")
    fun getRecentQueries(): Flow<List<YouTubeQueryHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(item: YouTubeQueryHistoryEntity)

    @Query("DELETE FROM youtube_recent_queries WHERE query = :query")
    suspend fun deleteQuery(query: String)

    @Query("DELETE FROM youtube_recent_queries WHERE query NOT IN (SELECT query FROM youtube_recent_queries ORDER BY timestamp DESC LIMIT :maxCount)")
    suspend fun trimQueries(maxCount: Int)

    @Query("DELETE FROM youtube_recent_queries")
    suspend fun clearQueries()
}
