package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.youtube.YouTubeSuggestionsProvider
import com.example.data.youtube.YouTubeVideoItem
import com.example.ui.components.YouTubeEmbeddedPlayer
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjCrimsonCue
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjObsidianBlack
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjTextTertiary
import com.example.viewmodel.SoundOperatorViewModel

@Composable
fun YouTubeScreen(
    viewModel: SoundOperatorViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.youTubeQuery.collectAsState()
    val searchResults by viewModel.youTubeSearchResults.collectAsState()
    val nextPageToken by viewModel.nextPageToken.collectAsState()
    val isSearching by viewModel.isYouTubeSearching.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val searchError by viewModel.youTubeSearchError.collectAsState()
    val activeVideo by viewModel.activeYouTubeVideo.collectAsState()
    val recentQueries by viewModel.youTubeRecentQueries.collectAsState()
    val suggestions by viewModel.youTubeSuggestions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val focusManager = LocalFocusManager.current

    // If an active video is playing, provide full dedicated in-app player view
    if (activeVideo != null) {
        BackHandler {
            viewModel.closeYouTubeVideo()
        }

        YouTubePlayerDetailView(
            video = activeVideo!!,
            onClose = { viewModel.closeYouTubeVideo() },
            onSwitchTrack = { newVideo -> viewModel.playYouTubeVideo(newVideo) },
            otherResults = searchResults.filter { it.videoId != activeVideo?.videoId }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjObsidianBlack)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // 1. Top Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DjCrimsonCue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "YOUTUBE PLAYER",
                        color = DjTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Official Embedded Player • Event Requests",
                        color = DjTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DjElevatedCard)
                    .border(1.dp, DjCrimsonCue, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "OFFICIAL API",
                    color = DjCrimsonCue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Search Input Field
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setYouTubeQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search songs, artists, wedding tracks...",
                    color = DjTextTertiary,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = DjCrimsonCue
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp),
                            color = DjCrimsonCue,
                            strokeWidth = 2.dp
                        )
                    }
                    if (query.isNotBlank()) {
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
                viewModel.searchYouTube(query, isManualSubmit = true)
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DjDeepSurface,
                unfocusedContainerColor = DjDeepSurface,
                focusedBorderColor = DjCrimsonCue,
                unfocusedBorderColor = DjBorderOutline,
                focusedTextColor = DjTextPrimary,
                unfocusedTextColor = DjTextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Category Quick Launch Chips (Mehndi, Baraat, Walima, Dance, Slow, Entry, DJ)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val padColor = try {
                    Color(android.graphics.Color.parseColor(category.colorHex))
                } catch (_: Exception) {
                    DjAmberGold
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DjElevatedCard)
                        .border(1.dp, padColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .clickable {
                            focusManager.clearFocus()
                            viewModel.searchYouTubeCategory(category.slug)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(padColor)
                        )
                        Text(
                            text = category.name,
                            color = DjTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Instant Search Suggestions (shown while typing or if recent exists)
        if (query.isNotBlank() && suggestions.isNotEmpty()) {
            Text(
                text = "SUGGESTIONS",
                color = DjTextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                items(suggestions) { suggestion ->
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
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = DjAmberGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = suggestion,
                                color = DjTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Recent Search Queries (Up to 20, with delete)
        if (recentQueries.isNotEmpty() && query.isBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT SEARCHES (${recentQueries.size})",
                    color = DjTextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Clear All",
                    color = DjTextTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { viewModel.clearYouTubeQueries() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(recentQueries) { item ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DjDeepSurface)
                            .border(1.dp, DjBorderOutline, RoundedCornerShape(16.dp))
                            .padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = DjTextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = item.query,
                            color = DjTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable {
                                focusManager.clearFocus()
                                viewModel.searchYouTube(item.query, isManualSubmit = true)
                            }
                        )
                        IconButton(
                            onClick = { viewModel.deleteYouTubeQuery(item.query) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove query",
                                tint = DjTextTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Error & Status Banner (with Retry)
        if (searchError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(containerColor = DjElevatedCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = DjAmberGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = searchError!!,
                        color = DjAmberGold,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.retryLastYouTubeSearch() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = DjAmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 7. Results Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SEARCH RESULTS (${searchResults.size})",
                color = DjTextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = DjCrimsonCue,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 8. Results List with Infinite Scroll / Load More
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(searchResults, key = { it.videoId }) { video ->
                YouTubeVideoCard(
                    video = video,
                    isActive = false,
                    onPlay = {
                        viewModel.playYouTubeVideo(video)
                    }
                )
            }

            // Pagination "Load More" Card
            if (nextPageToken != null && searchResults.isNotEmpty()) {
                item {
                    Button(
                        onClick = { viewModel.loadMoreYouTubeResults() },
                        enabled = !isLoadingMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DjElevatedCard,
                            contentColor = DjTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DjBorderOutline)
                    ) {
                        if (isLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = DjCrimsonCue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading more videos...", fontSize = 12.sp)
                        } else {
                            Text("Load More Results", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (searchResults.isEmpty() && !isSearching) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = DjTextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No results found",
                            color = DjTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Type in an artist, title, or wedding song to search YouTube.",
                            color = DjTextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated in-app official YouTube player screen.
 * Displays the 16:9 embedded player, title, channel, duration, and return controls.
 */
@Composable
fun YouTubePlayerDetailView(
    video: YouTubeVideoItem,
    onClose: () -> Unit,
    onSwitchTrack: (YouTubeVideoItem) -> Unit,
    otherResults: List<YouTubeVideoItem>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DjObsidianBlack)
            .padding(16.dp)
    ) {
        // Header with Back to Results
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Search",
                    tint = DjTextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NOW STREAMING",
                    color = DjCrimsonCue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Official YouTube Embedded Player",
                    color = DjTextSecondary,
                    fontSize = 10.sp
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Player",
                    tint = DjTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Official Embedded Player Frame
        YouTubeEmbeddedPlayer(
            video = video,
            onClose = onClose,
            onSwitchTrack = onSwitchTrack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Video Metadata Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DjElevatedCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = video.title,
                    color = DjTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.channelTitle,
                        color = DjAmberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DjDeepSurface)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = video.durationText,
                            color = DjTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Related / Next in Search Queue
        if (otherResults.isNotEmpty()) {
            Text(
                text = "MORE FROM SEARCH RESULTS",
                color = DjTextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(otherResults, key = { it.videoId }) { item ->
                    YouTubeVideoCard(
                        video = item,
                        isActive = false,
                        onPlay = { onSwitchTrack(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun YouTubeVideoCard(
    video: YouTubeVideoItem,
    isActive: Boolean,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) DjCrimsonCue else DjBorderOutline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DjElevatedCard else DjDeepSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail with duration overlay
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 66.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DjCrimsonCue.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Duration badge
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.durationText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info: Title, Channel, Official Badge
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = video.title,
                    color = if (isActive) DjAmberGold else DjTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Text(
                    text = video.channelTitle,
                    color = DjTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DjElevatedCard)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "YOUTUBE",
                            color = DjCrimsonCue,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (isActive) {
                        Text(
                            text = "PLAYING IN DECK",
                            color = DjNeonEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
