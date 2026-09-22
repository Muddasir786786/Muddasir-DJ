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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.entity.CategoryEntity
import com.example.data.entity.MusicPackEntity
import com.example.data.entity.PlaylistEntity
import com.example.ui.components.SongListItem
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

@Composable
fun LibraryScreen(
    viewModel: SoundOperatorViewModel,
    onCategoryClick: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val allSongs by viewModel.allSongs.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val musicPacks by viewModel.musicPacks.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val favoriteIds by viewModel.favoriteSongIds.collectAsState()

    val tabs = listOf("All Songs", "Categories", "Playlists", "Music Packs")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjObsidianBlack)
            .padding(top = 16.dp)
    ) {
        // Library Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Event Music Library",
                    color = DjTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allSongs.size} tracks offline • High-Fidelity",
                    color = DjTextSecondary,
                    fontSize = 12.sp
                )
            }

            if (selectedTab == 2) {
                IconButton(
                    onClick = { showCreatePlaylistDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DjAmberGold)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Playlist",
                        tint = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DjDeepSurface,
            contentColor = DjAmberGold,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = DjAmberGold,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) DjAmberGold else DjTextSecondary,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> { // All Songs
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allSongs, key = { it.id }) { song ->
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
            1 -> { // Categories
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (_: Exception) {
                            DjAmberGold
                        }
                        val count = allSongs.count {
                            it.title.contains(category.slug, true) ||
                                    it.cueNotes.contains(category.slug, true) ||
                                    (category.slug == "mehndi" && (it.title.contains("Mehndi", true) || it.title.contains("Jalebi", true))) ||
                                    (category.slug == "baraat" && (it.title.contains("Baraat", true) || it.title.contains("Dulha", true))) ||
                                    (category.slug == "walima" && it.title.contains("Walima", true)) ||
                                    (category.slug == "dance" && (it.title.contains("Dance", true) || it.bpm >= 120)) ||
                                    (category.slug == "slow" && (it.title.contains("Slow", true) || it.bpm <= 95)) ||
                                    (category.slug == "entry" && it.title.contains("Entry", true)) ||
                                    (category.slug == "dj" && (it.title.contains("DJ", true) || it.title.contains("Bass", true)))
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DjBorderOutline, RoundedCornerShape(14.dp))
                                .clickable { onCategoryClick(category) },
                            colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(catColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.name,
                                        color = DjTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = category.description,
                                        color = DjTextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$count Songs tagged",
                                        color = catColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        // Play first song in this category
                                        allSongs.firstOrNull { it.title.contains(category.slug, true) }?.let {
                                            viewModel.playSong(it, allSongs)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Category",
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> { // Playlists
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        val pColor = try {
                            Color(android.graphics.Color.parseColor(playlist.coverColorHex))
                        } catch (_: Exception) {
                            DjElectricCyan
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DjBorderOutline, RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(pColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = pColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        color = DjTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = playlist.description,
                                        color = DjTextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        allSongs.firstOrNull()?.let { viewModel.playSong(it, allSongs) }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(pColor)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Playlist",
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deletePlaylist(playlist) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = DjTextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> { // Music Packs
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(musicPacks, key = { it.id }) { pack ->
                        val packColor = try {
                            Color(android.graphics.Color.parseColor(pack.colorHex))
                        } catch (_: Exception) {
                            DjAmberGold
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DjBorderOutline, RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(packColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = packColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pack.title,
                                        color = DjTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = pack.subtitle,
                                        color = DjTextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${pack.eventType} • ${pack.songCount} Master Tracks",
                                        color = packColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = { viewModel.togglePackDownload(pack) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (pack.isDownloaded) DjDeepSurface else packColor,
                                        contentColor = if (pack.isDownloaded) DjNeonEmerald else Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (pack.isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (pack.isDownloaded) "READY" else "GET",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog for creating a new playlist
        if (showCreatePlaylistDialog) {
            var playlistTitle by remember { mutableStateOf("") }
            var playlistDesc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                title = { Text("Create Event Playlist", color = DjTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = playlistTitle,
                            onValueChange = { playlistTitle = it },
                            label = { Text("Playlist Name", color = DjTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = DjTextPrimary,
                                unfocusedTextColor = DjTextPrimary,
                                focusedBorderColor = DjAmberGold,
                                unfocusedBorderColor = DjBorderOutline
                            )
                        )
                        OutlinedTextField(
                            value = playlistDesc,
                            onValueChange = { playlistDesc = it },
                            label = { Text("Description (e.g. Baraat Entrance)", color = DjTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = DjTextPrimary,
                                unfocusedTextColor = DjTextPrimary,
                                focusedBorderColor = DjAmberGold,
                                unfocusedBorderColor = DjBorderOutline
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (playlistTitle.isNotBlank()) {
                                viewModel.createPlaylist(playlistTitle, playlistDesc)
                                showCreatePlaylistDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DjAmberGold, contentColor = Color.Black)
                    ) {
                        Text("Save Playlist", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) {
                        Text("Cancel", color = DjTextSecondary)
                    }
                },
                containerColor = DjElevatedCard
            )
        }
    }
}
