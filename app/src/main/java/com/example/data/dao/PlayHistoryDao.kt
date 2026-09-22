package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.PlayHistoryEntity
import com.example.data.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class PlayHistoryWithSong(
    val id: Long,
    val songId: Long,
    val playedAt: Long,
    val durationPlayedMs: Long,
    val eventName: String,
    val songTitle: String,
    val songArtist: String,
    val songDurationMs: Long,
    val songBpm: Int,
    val songCoverColorHex: String
)

@Dao
interface PlayHistoryDao {
    @Query("""
        SELECT ph.id, ph.songId, ph.playedAt, ph.durationPlayedMs, ph.eventName,
               s.title AS songTitle, s.artist AS songArtist, s.durationMs AS songDurationMs,
               s.bpm AS songBpm, s.coverColorHex AS songCoverColorHex
        FROM play_history ph
        INNER JOIN songs s ON ph.songId = s.id
        ORDER BY ph.playedAt DESC
        LIMIT 50
    """)
    fun getRecentHistory(): Flow<List<PlayHistoryWithSong>>

    @Query("""
        SELECT DISTINCT s.* FROM songs s
        INNER JOIN play_history ph ON s.id = ph.songId
        ORDER BY ph.playedAt DESC
        LIMIT 20
    """)
    fun getRecentlyPlayedSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlayHistoryEntity)

    @Query("DELETE FROM play_history")
    suspend fun clearHistory()
}
