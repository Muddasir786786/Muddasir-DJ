package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.entity.QueueItemEntity
import com.example.data.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class QueueItemWithSong(
    val id: Long,
    val songId: Long,
    val orderIndex: Int,
    val addedAt: Long,
    val songTitle: String,
    val songArtist: String,
    val songAlbum: String,
    val songDurationMs: Long,
    val songFilePath: String,
    val songBpm: Int,
    val songMusicalKey: String,
    val songCueNotes: String,
    val songCoverColorHex: String
)

@Dao
interface QueueDao {
    @Query("""
        SELECT q.id, q.songId, q.orderIndex, q.addedAt,
               s.title AS songTitle, s.artist AS songArtist, s.album AS songAlbum,
               s.durationMs AS songDurationMs, s.filePath AS songFilePath,
               s.bpm AS songBpm, s.musicalKey AS songMusicalKey,
               s.cueNotes AS songCueNotes, s.coverColorHex AS songCoverColorHex
        FROM queue_items q
        INNER JOIN songs s ON q.songId = s.id
        ORDER BY q.orderIndex ASC
    """)
    fun getQueueWithSongs(): Flow<List<QueueItemWithSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(queueItem: QueueItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(queueItems: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items WHERE id = :id")
    suspend fun deleteQueueItem(id: Long)

    @Query("DELETE FROM queue_items WHERE songId = :songId")
    suspend fun deleteQueueItemBySongId(songId: Long)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    @Query("SELECT MAX(orderIndex) FROM queue_items")
    suspend fun getMaxOrderIndex(): Int?

    @Transaction
    suspend fun replaceQueue(songIds: List<Long>) {
        clearQueue()
        val items = songIds.mapIndexed { index, songId ->
            QueueItemEntity(songId = songId, orderIndex = index)
        }
        insertQueueItems(items)
    }
}
