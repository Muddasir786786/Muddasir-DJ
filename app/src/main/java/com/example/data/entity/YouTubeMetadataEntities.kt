package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores local metadata history of viewed YouTube videos.
 * In accordance with YouTube policies, audiovisual content is NOT cached or saved.
 */
@Entity(tableName = "youtube_history")
data class YouTubeHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val durationText: String,
    val viewedAt: Long = System.currentTimeMillis()
)

/**
 * Stores recent YouTube search text queries locally for fast recall.
 */
@Entity(tableName = "youtube_recent_queries")
data class YouTubeQueryHistoryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
