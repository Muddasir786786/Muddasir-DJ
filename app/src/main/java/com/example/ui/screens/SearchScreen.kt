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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SongListItem
import com.example.ui.theme.LocalDjColors
import com.example.viewmodel.SoundOperatorViewModel

@Composable
fun SearchScreen(
    viewModel: SoundOperatorViewModel,
    onSearchYouTube: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSearchTab by remember { mutableIntStateOf(0) } // 0 = Unified All, 1 = Local Offline, 2 = Online YouTube

    val localSearchQuery by viewModel.searchQuery.collectAsState()
    val youTubeQuery by viewModel.youTubeQuery.collectAsState()

    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val localResults by viewModel.filteredSearchResults.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val favoriteIds by viewModel.favoriteSongIds.collectAsState()

    val youTubeResults by viewModel.youTubeSearchResults.collectAsState()
    val isYouTubeSearching by viewModel.isYouTubeSearching.collectAsState()
    val youTubeSuggestions by viewModel.youTubeSuggestions.collectAsState()
    val youTubeRecentQueries by viewModel.youTubeRecentQueries.collectAsState()

    val onlineMusicResults by viewModel.onlineMusicResults.collectAsState()
    val isOnlineMusicSearching by viewModel.isOnlineMusicSearching.collectAsState()
    val onlineMusicError by viewModel.onlineMusicError.collectAsState()

    val activeColors = LocalDjColors.current
    val focusManager = LocalFocusManager.current

    // Keep active search input unified
    val activeQuery = if (selectedSearchTab == 2) youTubeQuery else localSearchQuery

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(activeColors.background)
            .padding(top = 16.dp)
    ) {
        // Search Header Title & Subtitle
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Unified Music Search",
                color = activeColors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Find offline wedding tracks & stream online songs in one place",
                color = activeColors.textSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Unified Search Input Bar
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = activeQuery,
                onValueChange = { q ->
                    viewModel.setSearchQuery(q)
                    viewModel.setYouTubeQuery(q)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                placeholder = {
                    Text(
                        text = when (selectedSearchTab) {
                            1 -> "Search offline tracks, artists, BPM, cues..."
                            2 -> "Search online YouTube songs, requests..."
                            else -> "Search all local & online songs..."
                        },
                        color = activeColors.textTertiary,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = activeColors.primary
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isYouTubeSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp),
                                color = activeColors.primary,
                                strokeWidth = 2.dp
                            )
                        }
                        if (activeQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setYouTubeQuery("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = activeColors.textSecondary
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    if (activeQuery.isNotBlank()) {
                        viewModel.searchYouTube(activeQuery, isManualSubmit = true)
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = activeColors.surfaceElevated,
                    unfocusedContainerColor = activeColors.surfaceElevated,
                    focusedBorderColor = activeColors.primary,
                    unfocusedBorderColor = activeColors.border,
                    focusedTextColor = activeColors.textPrimary,
                    unfocusedTextColor = activeColors.textPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Clean Filter Tabs: Unified All, Local Offline, Online Music
        TabRow(
            selectedTabIndex = selectedSearchTab,
            containerColor = activeColors.surface,
            contentColor = activeColors.textPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSearchTab]),
                    height = 3.dp,
                    color = activeColors.primary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, activeColors.border, RoundedCornerShape(10.dp))
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
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (selectedSearchTab == 0) activeColors.primary else activeColors.textTertiary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "UNIFIED ALL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSearchTab == 0) activeColors.primary else activeColors.textTertiary
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
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = if (selectedSearchTab == 1) activeColors.primary else activeColors.textTertiary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "LOCAL (${localResults.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSearchTab == 1) activeColors.primary else activeColors.textTertiary
                        )
                    }
                }
            )

            Tab(
                selected = selectedSearchTab == 2,
                onClick = { selectedSearchTab = 2 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = if (selectedSearchTab == 2) activeColors.primary else activeColors.textTertiary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "ONLINE (${onlineMusicResults.size + youTubeResults.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSearchTab == 2) activeColors.primary else activeColors.textTertiary
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area based on Tab Selection
        when (selectedSearchTab) {
            0 -> {
                // ==========================================
                // TAB 0: UNIFIED ALL (BOTH LOCAL & ONLINE)
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // Quick Suggestions & Categories when query is empty
                    if (activeQuery.isBlank()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "POPULAR EVENT CATEGORIES",
                                    color = activeColors.textTertiary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(categories) { cat ->
                                        CategoryFilterChip(
                                            name = cat.name,
                                            isSelected = selectedCategory?.id == cat.id,
                                            colorHex = cat.colorHex,
                                            onClick = {
                                                viewModel.setSelectedCategoryFilter(cat)
                                                selectedSearchTab = 1
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (youTubeRecentQueries.isNotEmpty()) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "RECENT SEARCHES",
                                            color = activeColors.textTertiary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Clear",
                                            color = activeColors.textTertiary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.clickable { viewModel.clearYouTubeQueries() }
                                        )
                                    }

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(youTubeRecentQueries.take(6)) { item ->
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(activeColors.surface)
                                                    .border(1.dp, activeColors.border, RoundedCornerShape(16.dp))
                                                    .clickable {
                                                        focusManager.clearFocus()
                                                        viewModel.setSearchQuery(item.query)
                                                        viewModel.setYouTubeQuery(item.query)
                                                        viewModel.searchYouTube(item.query, isManualSubmit = true)
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.History, null, tint = activeColors.textTertiary, modifier = Modifier.size(12.dp))
                                                Text(item.query, color = activeColors.textSecondary, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- SECTION 1: LOCAL RESULTS ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.LibraryMusic, null, tint = activeColors.tertiary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "LOCAL SONGS",
                                    color = activeColors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(activeColors.tertiary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${localResults.size}",
                                        color = activeColors.tertiary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "SCREEN-OFF READY",
                                color = activeColors.tertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (localResults.isEmpty()) {
                        item {
                            Text(
                                text = "No local tracks match this search.",
                                color = activeColors.textTertiary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    } else {
                        items(localResults.take(6), key = { "local_${it.id}" }) { song ->
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
                    }

                    // --- SECTION 2: ONLINE STREAMING (BACKGROUND & SCREEN-OFF READY) ---
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CloudQueue, null, tint = activeColors.secondary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "ONLINE STREAMING",
                                    color = activeColors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(activeColors.secondary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${onlineMusicResults.size}",
                                        color = activeColors.secondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "SCREEN-OFF & BACKGROUND READY",
                                color = activeColors.secondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (onlineMusicResults.isEmpty() && !isOnlineMusicSearching) {
                        item {
                            Text(
                                text = onlineMusicError ?: "Type to search live online music tracks with background playback.",
                                color = activeColors.textTertiary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    } else {
                        items(onlineMusicResults.take(6), key = { "online_stream_${it.id}" }) { onlineSong ->
                            SongListItem(
                                song = onlineSong,
                                isPlaying = isPlaying && currentSong?.id == onlineSong.id,
                                isCurrentSong = currentSong?.id == onlineSong.id,
                                isFavorite = favoriteIds.contains(onlineSong.id),
                                onPlayClick = {
                                    viewModel.playSong(onlineSong, onlineMusicResults)
                                },
                                onFavoriteToggle = {
                                    viewModel.toggleFavorite(onlineSong.id)
                                },
                                onAddToQueue = {
                                    viewModel.addToQueue(onlineSong.id)
                                },
                                onPlayNext = {
                                    viewModel.playNextInQueue(onlineSong.id)
                                }
                            )
                        }
                    }

                    // --- SECTION 3: YOUTUBE OFFICIAL VIDEOS ---
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.SmartDisplay, null, tint = activeColors.primary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "YOUTUBE VIDEO CLIPS",
                                    color = activeColors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(activeColors.primary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${youTubeResults.size}",
                                        color = activeColors.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "FOREGROUND VIDEO",
                                color = activeColors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (youTubeResults.isEmpty() && !isYouTubeSearching) {
                        item {
                            Text(
                                text = "No YouTube videos matching query.",
                                color = activeColors.textTertiary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    } else {
                        items(youTubeResults.take(6), key = { "online_yt_${it.videoId}" }) { video ->
                            YouTubeVideoCard(
                                video = video,
                                isActive = false,
                                onPlay = {
                                    viewModel.playYouTubeVideo(video)
                                    onSearchYouTube(video.title)
                                }
                            )
                        }
                    }
                }
            }

            1 -> {
                // ==========================================
                // TAB 1: LOCAL ONLY WITH CATEGORY CHIPS
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${localResults.size} offline local tracks found",
                            color = activeColors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (selectedCategory != null) {
                            Text(
                                text = "Category: ${selectedCategory?.name}",
                                color = activeColors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

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
                                    Icon(Icons.Default.MusicOff, null, tint = activeColors.textTertiary, modifier = Modifier.size(48.dp))
                                    Text(
                                        text = "No local tracks match your query",
                                        color = activeColors.textSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // ==========================================
                // TAB 2: ONLINE MUSIC (BACKGROUND STREAMING & YOUTUBE)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Event Presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(com.example.data.online.OnlineMusicClient.curatedEventPresets) { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(activeColors.surface)
                                    .border(1.dp, activeColors.border, RoundedCornerShape(16.dp))
                                    .clickable {
                                        focusManager.clearFocus()
                                        viewModel.setSearchQuery(preset)
                                        viewModel.setYouTubeQuery(preset)
                                        viewModel.searchOnlineMusic(preset)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CloudQueue, null, tint = activeColors.secondary, modifier = Modifier.size(13.dp))
                                    Text(preset, color = activeColors.textSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Section 1: Online Background Audio
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Online Audio Streams (${onlineMusicResults.size})",
                                    color = activeColors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "BACKGROUND & SCREEN-OFF READY",
                                    color = activeColors.secondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (onlineMusicResults.isEmpty() && !isOnlineMusicSearching) {
                            item {
                                Text(
                                    text = onlineMusicError ?: "Type above to search millions of online tracks with background playback.",
                                    color = activeColors.textTertiary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        } else {
                            items(onlineMusicResults, key = { "tab2_online_${it.id}" }) { onlineSong ->
                                SongListItem(
                                    song = onlineSong,
                                    isPlaying = isPlaying && currentSong?.id == onlineSong.id,
                                    isCurrentSong = currentSong?.id == onlineSong.id,
                                    isFavorite = favoriteIds.contains(onlineSong.id),
                                    onPlayClick = {
                                        viewModel.playSong(onlineSong, onlineMusicResults)
                                    },
                                    onFavoriteToggle = {
                                        viewModel.toggleFavorite(onlineSong.id)
                                    },
                                    onAddToQueue = {
                                        viewModel.addToQueue(onlineSong.id)
                                    },
                                    onPlayNext = {
                                        viewModel.playNextInQueue(onlineSong.id)
                                    }
                                )
                            }
                        }

                        // Section 2: YouTube Video Clips
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "YouTube Video Clips (${youTubeResults.size})",
                                    color = activeColors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "FOREGROUND VIDEO PLAYER",
                                    color = activeColors.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(youTubeResults, key = { "tab2_yt_${it.videoId}" }) { video ->
                            YouTubeVideoCard(
                                video = video,
                                isActive = false,
                                onPlay = {
                                    viewModel.playYouTubeVideo(video)
                                    onSearchYouTube(video.title)
                                }
                            )
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
    val activeColors = LocalDjColors.current
    val chipColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        activeColors.primary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) chipColor else activeColors.surfaceElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) chipColor else activeColors.border,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.Black else activeColors.textPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
