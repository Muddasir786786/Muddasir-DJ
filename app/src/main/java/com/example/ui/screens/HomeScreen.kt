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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CategoryEntity
import com.example.data.entity.MusicPackEntity
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
fun HomeScreen(
    viewModel: SoundOperatorViewModel,
    onCategoryClick: (CategoryEntity) -> Unit,
    onSearchClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val musicPacks by viewModel.musicPacks.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedSongs.collectAsState()
    val favoriteIds by viewModel.favoriteSongIds.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val featuredSongs = allSongs.take(8)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DjObsidianBlack),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. TOP HEADER: Clean logo, status badge, and settings icon
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DjAmberGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Logo",
                            tint = DjObsidianBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "SOUND OPERATOR",
                            color = DjTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DjNeonEmerald)
                            )
                            Text(
                                text = "LIVE EVENT CONSOLE",
                                color = DjTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DjDeepSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "More & Settings",
                        tint = DjTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. SEARCH: Large prominent search bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DjDeepSurface)
                    .border(1.dp, DjBorderOutline, RoundedCornerShape(14.dp))
                    .clickable { onSearchClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = DjAmberGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Search songs, artists, wedding tracks...",
                        color = DjTextTertiary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 3. CATEGORIES: Compact horizontal chips (Mehndi, Baraat, Walima, Dance, Slow, Entry, DJ)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORIES",
                        color = DjTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (_: Exception) {
                            DjAmberGold
                        }

                        val icon = getCategoryIcon(category.slug)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DjDeepSurface)
                                .border(1.dp, catColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable { onCategoryClick(category) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = category.name,
                                color = DjTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. FEATURED CAROUSEL: Modern horizontal cards with artwork & 1-tap play
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FEATURED TRACKS",
                        color = DjTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredSongs, key = { it.id }) { song ->
                        val isCurrent = currentSong?.id == song.id
                        FeaturedSongCard(
                            song = song,
                            isCurrent = isCurrent,
                            isPlaying = isCurrent && isPlaying,
                            onPlayClick = { viewModel.playSong(song, featuredSongs) }
                        )
                    }
                }
            }
        }

        // 5. MUSIC PACKS: Compact preview with "View All"
        if (musicPacks.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EVENT PACKS",
                            color = DjTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        TextButton(
                            onClick = onLibraryClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "View All",
                                color = DjAmberGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(musicPacks.take(4), key = { it.id }) { pack ->
                            MusicPackCompactCard(
                                pack = pack,
                                onClick = onLibraryClick
                            )
                        }
                    }
                }
            }
        }

        // 6. RECENTLY PLAYED / QUICK ACCESS: Clean compact list
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                val songsToShow = if (recentlyPlayed.isNotEmpty()) recentlyPlayed.take(5) else allSongs.drop(4).take(5)

                Text(
                    text = if (recentlyPlayed.isNotEmpty()) "RECENTLY PLAYED" else "READY IN QUEUE",
                    color = DjTextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    songsToShow.forEach { song ->
                        val isCurrent = currentSong?.id == song.id
                        val isFav = favoriteIds.contains(song.id)

                        HomeSongListItem(
                            song = song,
                            isCurrent = isCurrent,
                            isPlaying = isCurrent && isPlaying,
                            isFavorite = isFav,
                            onPlayClick = { viewModel.playSong(song, songsToShow) },
                            onFavoriteClick = { viewModel.toggleFavorite(song.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedSongCard(
    song: SongEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    val coverColor = try {
        Color(android.graphics.Color.parseColor(song.coverColorHex))
    } catch (_: Exception) {
        DjAmberGold
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = if (isCurrent) DjAmberGold else DjBorderOutline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onPlayClick() },
        colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Artwork Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(coverColor.copy(alpha = 0.25f))
                    .border(1.dp, coverColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent && isPlaying) DjAmberGold else DjDeepSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isCurrent && isPlaying) DjObsidianBlack else DjAmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = song.title,
                color = DjTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                color = DjTextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${song.bpm} BPM",
                    color = DjAmberGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatDuration(song.durationMs),
                    color = DjTextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun MusicPackCompactCard(
    pack: MusicPackEntity,
    onClick: () -> Unit
) {
    val packColor = try {
        Color(android.graphics.Color.parseColor(pack.colorHex))
    } catch (_: Exception) {
        DjElectricCyan
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DjBorderOutline, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DjDeepSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(packColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderZip,
                    contentDescription = null,
                    tint = packColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pack.title,
                    color = DjTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${pack.songCount} offline tracks",
                    color = DjTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun HomeSongListItem(
    song: SongEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = if (isCurrent) DjAmberGold else DjBorderOutline,
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent && isPlaying) DjAmberGold else DjElevatedCard)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = if (isCurrent && isPlaying) DjObsidianBlack else DjAmberGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = if (isCurrent) DjAmberGold else DjTextPrimary,
                    fontSize = 13.sp,
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
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• ${formatDuration(song.durationMs)}",
                        color = DjTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) DjCrimsonCue else DjTextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun getCategoryIcon(slug: String): ImageVector = when (slug.lowercase()) {
    "mehndi" -> Icons.Default.Celebration
    "baraat" -> Icons.Default.Campaign
    "walima" -> Icons.Default.Nightlife
    "dance" -> Icons.Default.SpeakerGroup
    "slow" -> Icons.Default.Favorite
    "entry" -> Icons.Default.Stars
    "dj" -> Icons.Default.Equalizer
    else -> Icons.Default.GraphicEq
}
