package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SongEntity
import com.example.ui.components.formatDuration
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElectricCyan
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjObsidianBlack
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjTextTertiary
import com.example.viewmodel.SoundOperatorViewModel

@Composable
fun CategoryDetailScreen(
    categorySlug: String,
    viewModel: SoundOperatorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val favoriteSongIds by viewModel.favoriteSongIds.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val currentCategory = categories.firstOrNull { it.slug.equals(categorySlug, ignoreCase = true) }
    val categoryName = currentCategory?.name ?: categorySlug.replaceFirstChar { it.uppercase() }

    val categoryColor = try {
        if (currentCategory != null) Color(android.graphics.Color.parseColor(currentCategory.colorHex))
        else DjAmberGold
    } catch (_: Exception) {
        DjAmberGold
    }

    var filterQuery by remember { mutableStateOf("") }

    val categorySongs = allSongs.filter { song ->
        val slug = categorySlug.lowercase()
        val matchesCategory = song.title.contains(slug, ignoreCase = true) ||
                song.cueNotes.contains(slug, ignoreCase = true) ||
                song.album.contains(slug, ignoreCase = true) ||
                (slug == "mehndi" && (song.title.contains("Mehndi", ignoreCase = true) || song.title.contains("Jalebi", ignoreCase = true) || song.title.contains("Sangeet", ignoreCase = true))) ||
                (slug == "baraat" && (song.title.contains("Baraat", ignoreCase = true) || song.title.contains("Dhol", ignoreCase = true) || song.title.contains("Dulha", ignoreCase = true))) ||
                (slug == "walima" && (song.title.contains("Walima", ignoreCase = true) || song.title.contains("Reception", ignoreCase = true))) ||
                (slug == "dance" && (song.title.contains("Dance", ignoreCase = true) || song.title.contains("Bhangra", ignoreCase = true) || song.bpm >= 120)) ||
                (slug == "slow" && (song.title.contains("Slow", ignoreCase = true) || song.title.contains("Acoustic", ignoreCase = true) || song.bpm <= 95)) ||
                (slug == "entry" && (song.title.contains("Entry", ignoreCase = true) || song.title.contains("Entrance", ignoreCase = true) || song.title.contains("Arrival", ignoreCase = true))) ||
                (slug == "dj" && (song.title.contains("DJ", ignoreCase = true) || song.title.contains("Bass", ignoreCase = true) || song.title.contains("Drop", ignoreCase = true)))

        if (filterQuery.isBlank()) {
            matchesCategory
        } else {
            matchesCategory && (
                    song.title.contains(filterQuery, ignoreCase = true) ||
                            song.artist.contains(filterQuery, ignoreCase = true) ||
                            song.album.contains(filterQuery, ignoreCase = true)
                    )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjObsidianBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Row with Back Button, Category Title, Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DjDeepSurface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DjTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(categoryColor)
                        )
                        Text(
                            text = categoryName.uppercase(),
                            color = DjTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "${categorySongs.size} tracks available offline",
                        color = DjTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(categoryColor.copy(alpha = 0.15f))
                    .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "OFFLINE",
                    color = categoryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // In-Category Filter Bar
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Filter $categoryName songs...",
                    color = DjTextTertiary,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (filterQuery.isNotEmpty()) {
                    IconButton(onClick = { filterQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = DjTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DjDeepSurface,
                unfocusedContainerColor = DjDeepSurface,
                focusedBorderColor = categoryColor,
                unfocusedBorderColor = DjBorderOutline,
                focusedTextColor = DjTextPrimary,
                unfocusedTextColor = DjTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Song List
        if (categorySongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = if (filterQuery.isNotEmpty()) "No tracks match '$filterQuery'" else "No tracks in this category",
                    color = DjTextTertiary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(categorySongs, key = { it.id }) { song ->
                    val isCurrent = currentSong?.id == song.id
                    val isFav = favoriteSongIds.contains(song.id)

                    CategorySongCard(
                        song = song,
                        isCurrent = isCurrent,
                        isPlaying = isCurrent && isPlaying,
                        isFavorite = isFav,
                        accentColor = categoryColor,
                        onPlayClick = { viewModel.playSong(song, categorySongs) },
                        onFavoriteClick = { viewModel.toggleFavorite(song.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySongCard(
    song: SongEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    accentColor: Color,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = if (isCurrent) accentColor else DjBorderOutline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onPlayClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) DjElevatedCard else DjDeepSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Play Button Thumbnail
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent && isPlaying) accentColor else DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = if (isCurrent && isPlaying) Color.Black else accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Song Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = if (isCurrent) accentColor else DjTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = song.artist,
                        color = DjTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "• ${song.bpm} BPM",
                        color = DjAmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• ${formatDuration(song.durationMs)}",
                        color = DjTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            // Favorite Button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) DjCrimsonCue else DjTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
