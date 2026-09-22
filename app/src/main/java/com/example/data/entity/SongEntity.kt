package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String = "Event Collection",
    val durationMs: Long,
    val filePath: String,
    val bpm: Int = 120,
    val musicalKey: String = "8A / Am",
    val cueNotes: String = "",
    val coverColorHex: String = "#FF9800",
    val isLocal: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
