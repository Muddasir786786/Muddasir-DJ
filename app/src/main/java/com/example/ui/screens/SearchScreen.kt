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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CategoryEntity
import com.example.ui.components.SongListItem
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjHighlightCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjObsidianBlack
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjTextTertiary
import com.example.viewmodel.SoundOperatorViewModel

@Composable
fun SearchScreen(
    viewModel: SoundOperatorViewModel,
    onSearchYouTube: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSearchTab by remember { mutableIntStateOf(0) } // 0 = Local Library, 1 = YouTube Search

    val localSearchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val localResults by viewModel.filteredSearchResults.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val favoriteIds by viewModel.favoriteSongIds.collectAsState()

    // YouTube State
    val youTubeQuery by viewModel.youTubeQuery.collectAsState()
    val youTubeResults by viewModel.youTubeSearchResults.collectAsState()
    val isYouTubeSearching by viewModel.isYouTubeSearching.collectAsState()
    val youTubeSuggestions by viewModel.youTubeSuggestions.collectAsState()
    val youTubeRecentQueries by viewModel.youTubeRecentQueries.collectAsState()
    val youTubeError by viewModel.youTubeSearchError.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjObsidianBlack)
            .padding(top = 16.dp)
    ) {
        // Search Screen Title
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Fast Music Search",
                color = DjTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Distinct Dual Tabs: Local Library vs Official YouTube Search
            TabRow(
                selectedTabIndex = selectedSearchTab,
                containerColor = DjDeepSurface,
                contentColor = DjTextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSearchTab]),
                        height = 3.dp,
                        color = if (selectedSearchTab == 0) DjAmberGold else DjCrimsonCue
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, DjBorderOutline, RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedSearchTab == 0,
                    onClick = { selectedSearchTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = if (selectedSearchTab == 0) DjAmberGold else DjTextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "LOCAL LIBRARY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSearchTab == 0) DjAmberGold else DjTextTertiary
                            )
                        }
                    }
                )

                Tab(
                    selected = selectedSearchTab == 1,
                    onClick = { selectedSearchTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = null,
                                tint = if (selectedSearchTab == 1) DjCrimsonCue else DjTextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "YOUTUBE SEARCH",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSearchTab == 1) DjCrimsonCue else DjTextTertiary
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedSearchTab == 0) {
            // ==========================================
            // TAB 0: OFFLINE LOCAL LIBRARY SEARCH
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Local Search Input
                OutlinedTextField(
                    value = localSearchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    placeholder = {
                        Text("Search title, artist, BPM, key, cues...", color = DjTextTertiary, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Local",
                            tint = DjAmberGold
                        )
                    },
                    trailingIcon = {
                        if (localSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = DjTextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DjElevatedCard,
                        unfocusedContainerColor = DjElevatedCard,
                        focusedBorderColor = DjAmberGold,
                        unfocusedBorderColor = DjBorderOutline,
                        focusedTextColor = DjTextPrimary,
                        unfocusedTextColor = DjTextPrimary
                    )
                )

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val isAllSelected = selectedCategory == null
                        CategoryFilterChip(
                            name = "All Events",
                            isSelected = isAllSelected,
                            colorHex = "#FFB300",
                            onClick = { viewModel.setSelectedCategoryFilter(null) }
                        )
                    }

                    items(categories) { category ->
                        val isSelected = selectedCategory?.id == category.id
                        CategoryFilterChip(
                            name = category.name,
                            isSelected = isSelected,
                            colorHex = category.colorHex,
                            onClick = {
                                if (isSelected) {
                                    viewModel.setSelectedCategoryFilter(null)
                                } else {
                                    viewModel.setSelectedCategoryFilter(category)
                                }
                            }
                        )
                    }
                }

                // YouTube Bridge Switcher Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, DjCrimsonCue.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable {
                            if (localSearchQuery.isNotBlank()) {
                                viewModel.searchYouTube(localSearchQuery, isManualSubmit = true)
                            }
                            selectedSearchTab = 1
                        },
                    colors = CardDefaults.cardColors(containerColor = DjElevatedCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = null,
                                tint = DjCrimsonCue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (localSearchQuery.isNotBlank()) "Can't find local track? Search YouTube for \"$localSearchQuery\"" else "Switch to Official YouTube Search",
                                color = DjTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "SEARCH ↗",
                            color = DjCrimsonCue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Results count status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${localResults.size} offline local tracks found",
                        color = DjTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (selectedCategory != null) {
                        Text(
                            text = "Category: ${selectedCategory?.name}",
                            color = DjAmberGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Local Song Results List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(localResults, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = isPlaying && currentSong?.id == song.id,
                            isCurrentSong = currentSong?.id == song.id,
                            isFavorite = favoriteIds.contains(song.id),
                            onPlayClick = { viewModel.playSong(song, localResults) },
                            onFavoriteToggle = { viewModel.toggleFavorite(song.id) },
                            onAddToQueue = { viewModel.addToQueue(song.id) },
                            onPlayNext = { viewModel.playNextInQueue(song.id) }
                        )
                    }

                    if (localResults.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicOff,
                                    contentDescription = null,
                                    tint = DjTextTertiary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No local tracks match your query",
                                    color = DjTextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Try clearing filters or search YouTube above.",
                                    color = DjTextTertiary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // TAB 1: OFFICIAL YOUTUBE SEARCH
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // YouTube Search Bar
                OutlinedTextField(
                    value = youTubeQuery,
                    onValueChange = { viewModel.setYouTubeQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    placeholder = {
                        Text("Search YouTube for songs, artists, requests...", color = DjTextTertiary, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search YouTube",
                            tint = DjCrimsonCue
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isYouTubeSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp),
                                    color = DjCrimsonCue,
                                    strokeWidth = 2.dp
                                )
                            }
                            if (youTubeQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setYouTubeQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = DjTextSecondary
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        viewModel.searchYouTube(youTubeQuery, isManualSubmit = true)
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DjElevatedCard,
                        unfocusedContainerColor = DjElevatedCard,
                        focusedBorderColor = DjCrimsonCue,
                        unfocusedBorderColor = DjBorderOutline,
                        focusedTextColor = DjTextPrimary,
                        unfocusedTextColor = DjTextPrimary
                    )
                )

                // Instant Suggestions
                if (youTubeQuery.isNotBlank() && youTubeSuggestions.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(youTubeSuggestions) { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DjDeepSurface)
                                    .border(1.dp, DjBorderOutline, RoundedCornerShape(16.dp))
                                    .clickable {
                                        focusManager.clearFocus()
                                        viewModel.searchYouTube(suggestion, isManualSubmit = true)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.TrendingUp, null, tint = DjAmberGold, modifier = Modifier.size(13.dp))
                                    Text(suggestion, color = DjTextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Recent YouTube Searches
                if (youTubeRecentQueries.isNotEmpty() && youTubeQuery.isBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RECENT SEARCHES", color = DjTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Clear", color = DjTextTertiary, fontSize = 10.sp, modifier = Modifier.clickable { viewModel.clearYouTubeQueries() })
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(youTubeRecentQueries.take(8)) { item ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DjDeepSurface)
                                    .border(1.dp, DjBorderOutline, RoundedCornerShape(16.dp))
                                    .clickable {
                                        focusManager.clearFocus()
                                        viewModel.searchYouTube(item.query, isManualSubmit = true)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.History, null, tint = DjTextTertiary, modifier = Modifier.size(12.dp))
                                Text(item.query, color = DjTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // YouTube Results count status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Official YouTube Results (${youTubeResults.size})",
                        color = DjTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "TAP TO PLAY IN DECK",
                        color = DjCrimsonCue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // YouTube Results List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(youTubeResults, key = { it.videoId }) { video ->
                        YouTubeVideoCard(
                            video = video,
                            isActive = false,
                            onPlay = {
                                viewModel.playYouTubeVideo(video)
                                onSearchYouTube(video.title)
                            }
                        )
                    }

                    if (youTubeResults.isEmpty() && !isYouTubeSearching) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartDisplay,
                                    contentDescription = null,
                                    tint = DjTextTertiary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Search YouTube to find live tracks",
                                    color = DjTextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilterChip(
    name: String,
    isSelected: Boolean,
    colorHex: String,
    onClick: () -> Unit
) {
    val chipColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        DjAmberGold
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) chipColor else DjElevatedCard)
            .border(
                width = 1.dp,
                color = if (isSelected) chipColor else DjBorderOutline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.Black else DjTextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
