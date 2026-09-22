package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CategoryEntity
import com.example.data.entity.SongEntity
import com.example.ui.components.AudioOutputBadge
import com.example.ui.components.DJVisualizer
import com.example.ui.components.SongListItem
import androidx.compose.material.icons.filled.SmartDisplay
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElectricCyan
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjHighlightCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjObsidianBlack
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjTextTertiary
import com.example.viewmodel.SoundOperatorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: SoundOperatorViewModel,
    onCategoryClick: (CategoryEntity) -> Unit,
    onSearchClick: () -> Unit,
    onYouTubeClick: () -> Unit,
    onLibraryClick: () -> Unit = {},
    onNowPlayingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isMasterMuted by viewModel.isMasterMuted.collectAsState()
    val favoriteIds by viewModel.favoriteSongIds.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedSongs.collectAsState()
    val audioOutputStatus by viewModel.audioOutputStatus.collectAsState()
    val visualizerBands by viewModel.visualizerBands.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DjObsidianBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Stage Sound Booth Header & Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) DjNeonEmerald else DjAmberGold)
                            )
                            Text(
                                text = "SOUND OPERATOR",
                                color = DjAmberGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        }
                        Text(
                            text = "Stage Sound Booth",
                            color = DjTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Emergency Master Mute Button
                    IconButton(
                        onClick = { viewModel.toggleMasterMute() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isMasterMuted) DjCrimsonCue else DjElevatedCard)
                            .border(1.dp, if (isMasterMuted) DjCrimsonCue else DjBorderOutline, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMasterMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Master Mute",
                            tint = if (isMasterMuted) Color.White else DjAmberGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Audio Output Detection Bar (Bluetooth/AUX/Speaker)
                AudioOutputBadge(
                    outputStatus = audioOutputStatus,
                    onRefresh = { viewModel.refreshAudioOutput() },
                    modifier = Modifier.fillMaxWidth()
                )

                // Entry Point Decks: Official YouTube + Local Offline Library
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Official YouTube Live Request Deck Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DjCrimsonCue.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .clickable { onYouTubeClick() },
                        colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(DjCrimsonCue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartDisplay,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "LIVE",
                                    color = DjCrimsonCue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column {
                                Text(
                                    text = "YOUTUBE",
                                    color = DjTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Stream guest requests",
                                    color = DjTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Local Offline Library Entry Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DjAmberGold.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .clickable { onLibraryClick() },
                        colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(DjAmberGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LibraryMusic,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "${allSongs.size} TRACKS",
                                    color = DjAmberGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column {
                                Text(
                                    text = "OFFLINE LIBRARY",
                                    color = DjTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cues, BPM & zero lag",
                                    color = DjTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Event Category Quick Launch Pads (7 Wedding & Event categories)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EVENT CATEGORIES",
                        color = DjTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${categories.size} Event Pads",
                        color = DjAmberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 2
                ) {
                    categories.forEach { category ->
                        EventCategoryPad(
                            category = category,
                            songCount = allSongs.count {
                                it.title.contains(category.slug, ignoreCase = true) ||
                                        it.cueNotes.contains(category.slug, ignoreCase = true) ||
                                        (category.slug == "mehndi" && (it.title.contains("Mehndi", true) || it.title.contains("Jalebi", true))) ||
                                        (category.slug == "baraat" && (it.title.contains("Baraat", true) || it.title.contains("Dulha", true))) ||
                                        (category.slug == "walima" && it.title.contains("Walima", true)) ||
                                        (category.slug == "dance" && (it.title.contains("Dance", true) || it.bpm >= 120)) ||
                                        (category.slug == "slow" && (it.title.contains("Slow", true) || it.bpm <= 95)) ||
                                        (category.slug == "entry" && it.title.contains("Entry", true)) ||
                                        (category.slug == "dj" && (it.title.contains("DJ", true) || it.title.contains("Bass", true)))
                            },
                            onClick = { onCategoryClick(category) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Quick Cue Jingles / Sound Bites Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "HOT CUE SOUND BITES",
                    color = DjTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HotCuePad(
                        title = "Dhol Blast",
                        subtitle = "Baraat Gate",
                        color = DjCrimsonCue,
                        onClick = {
                            allSongs.firstOrNull { it.title.contains("Dhol", true) }?.let {
                                viewModel.playSong(it, allSongs)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    HotCuePad(
                        title = "Bride Entry",
                        subtitle = "Shehnai Fanfare",
                        color = DjAmberGold,
                        onClick = {
                            allSongs.firstOrNull { it.title.contains("Bride", true) }?.let {
                                viewModel.playSong(it, allSongs)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    HotCuePad(
                        title = "Bass Drop",
                        subtitle = "DJ Floor Drop",
                        color = DjElectricCyan,
                        onClick = {
                            allSongs.firstOrNull { it.title.contains("Party", true) }?.let {
                                viewModel.playSong(it, allSongs)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Quick Playlists Row
        if (playlists.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "EVENT PLAYLISTS",
                        color = DjTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(playlists) { playlist ->
                            PlaylistCard(
                                playlistName = playlist.name,
                                description = playlist.description,
                                colorHex = playlist.coverColorHex,
                                onClick = {
                                    // Play first song in this event set
                                    allSongs.firstOrNull()?.let { viewModel.playSong(it, allSongs) }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Recently Played Tracks
        if (recentlyPlayed.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RECENTLY PLAYED ON STAGE",
                        color = DjTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentlyPlayed.take(4).forEach { song ->
                            SongListItem(
                                song = song,
                                isPlaying = isPlaying && currentSong?.id == song.id,
                                isCurrentSong = currentSong?.id == song.id,
                                isFavorite = favoriteIds.contains(song.id),
                                onPlayClick = { viewModel.playSong(song, allSongs) },
                                onFavoriteToggle = { viewModel.toggleFavorite(song.id) },
                                onAddToQueue = { viewModel.addToQueue(song.id) },
                                onPlayNext = { viewModel.playNextInQueue(song.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCategoryPad(
    category: CategoryEntity,
    songCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val padColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (_: Exception) {
        DjAmberGold
    }

    val iconVector: ImageVector = when (category.slug.lowercase()) {
        "mehndi" -> Icons.Default.Celebration
        "baraat" -> Icons.Default.Campaign
        "walima" -> Icons.Default.Nightlife
        "dance" -> Icons.Default.SpeakerGroup
        "slow" -> Icons.Default.Favorite
        "entry" -> Icons.Default.Stars
        "dj" -> Icons.Default.Equalizer
        else -> Icons.Default.LibraryMusic
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, padColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = DjElevatedCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(padColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = category.name,
                    tint = padColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    color = DjTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$songCount Tracks",
                    color = padColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun HotCuePad(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DjElevatedCard)
            .border(1.5.dp, color.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    color = DjTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = subtitle,
                color = DjTextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlaylistCard(
    playlistName: String,
    description: String,
    colorHex: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        DjElectricCyan
    }

    Box(
        modifier = modifier
            .width(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DjElevatedCard)
            .border(1.dp, DjBorderOutline, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = playlistName,
                color = DjTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = description,
                color = DjTextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
