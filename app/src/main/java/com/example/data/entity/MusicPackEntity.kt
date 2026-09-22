package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_packs")
data class MusicPackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String,
    val eventType: String,
    val songCount: Int,
    val isDownloaded: Boolean = true,
    val colorHex: String = "#FFB300"
)
