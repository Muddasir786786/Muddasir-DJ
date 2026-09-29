package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CategoryDao
import com.example.data.dao.FavoriteDao
import com.example.data.dao.MusicPackDao
import com.example.data.dao.PlayHistoryDao
import com.example.data.dao.PlaylistDao
import com.example.data.dao.QueueDao
import com.example.data.dao.SettingDao
import com.example.data.dao.SongDao
import com.example.data.dao.YouTubeMetadataDao
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
import com.example.data.entity.YouTubeHistoryEntity
import com.example.data.entity.YouTubeQueryHistoryEntity

@Database(
    entities = [
        SongEntity::class,
        CategoryEntity::class,
        SongCategoryEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        FavoriteEntity::class,
        PlayHistoryEntity::class,
        QueueItemEntity::class,
        SettingEntity::class,
        MusicPackEntity::class,
        YouTubeHistoryEntity::class,
        YouTubeQueryHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun categoryDao(): CategoryDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun queueDao(): QueueDao
    abstract fun settingDao(): SettingDao
    abstract fun musicPackDao(): MusicPackDao
    abstract fun youTubeMetadataDao(): YouTubeMetadataDao

    companion object {
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN contentHash TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE songs ADD COLUMN source TEXT NOT NULL DEFAULT 'local'")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sound_operator_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
