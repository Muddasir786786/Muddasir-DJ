package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.MusicPackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicPackDao {
    @Query("SELECT * FROM music_packs ORDER BY id ASC")
    fun getAllMusicPacks(): Flow<List<MusicPackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusicPacks(packs: List<MusicPackEntity>)

    @Update
    suspend fun updateMusicPack(pack: MusicPackEntity)

    @Query("SELECT COUNT(*) FROM music_packs")
    suspend fun getPackCount(): Int
}
