package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CategoryEntity
import com.example.data.entity.SongCategoryEntity
import com.example.data.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    fun getSongFlowById(id: Long): Flow<SongEntity?>

    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%'
           OR cueNotes LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_categories sc ON s.id = sc.songId
        WHERE sc.categoryId = :categoryId
        ORDER BY s.title ASC
    """)
    fun getSongsByCategoryId(categoryId: Long): Flow<List<SongEntity>>

    @Query("""
        SELECT c.* FROM categories c
        INNER JOIN song_categories sc ON c.id = sc.categoryId
        WHERE sc.songId = :songId
        ORDER BY c.displayOrder ASC
    """)
    fun getCategoriesForSong(songId: Long): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>): List<Long>

    @Update
    suspend fun updateSong(song: SongEntity)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongCategory(songCategory: SongCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongCategories(songCategories: List<SongCategoryEntity>)

    @Query("DELETE FROM song_categories WHERE songId = :songId")
    suspend fun clearCategoriesForSong(songId: Long)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
}
