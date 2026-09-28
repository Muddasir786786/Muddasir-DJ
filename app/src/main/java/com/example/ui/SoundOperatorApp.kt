package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.YouTubeMiniPlayerBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.QueueScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.YouTubePlayerDetailView
import com.example.ui.screens.YouTubeScreen
import com.example.ui.theme.DjAmberGold
import com.example.ui.theme.DjBorderOutline
import com.example.ui.theme.DjDeepSurface
import com.example.ui.theme.DjElevatedCard
import com.example.ui.theme.DjNeonEmerald
import com.example.ui.theme.DjObsidianBlack
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjTextTertiary
import com.example.viewmodel.SoundOperatorViewModel

@Composable
fun SoundOperatorApp(
    viewModel: SoundOperatorViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val visualizerBands by viewModel.visualizerBands.collectAsState()
    val isMasterMuted by viewModel.isMasterMuted.collectAsState()

    val activeYouTubeVideo by viewModel.activeYouTubeVideo.collectAsState()
    val isYouTubePlaying by com.example.playback.YouTubePlayerBridge.isPlaying.collectAsState()
    val isYouTubeExpanded by viewModel.isYouTubeExpanded.collectAsState()
    val youTubeSearchResults by viewModel.youTubeSearchResults.collectAsState()

    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    // Intercept back button when Now Playing is full-screen
    BackHandler(enabled = isNowPlayingExpanded) {
        isNowPlayingExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize().background(DjObsidianBlack)) {
        Scaffold(
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DjObsidianBlack)
                ) {
                    // Docked Mini-Player Bar (shown when song or video is playing and full player is not expanded)
                    if (activeYouTubeVideo != null && !isYouTubeExpanded) {
                        YouTubeMiniPlayerBar(
                            video = activeYouTubeVideo!!,
                            isPlaying = isYouTubePlaying,
                            onPlayPauseClick = { viewModel.toggleYouTubePlayPause() },
                            onCloseClick = { viewModel.stopYouTubePlayback() },
                            onExpandClick = { viewModel.expandYouTubePlayer() },
                            onNextClick = { viewModel.playNextYouTubeVideo() }
                        )
                    } else if (currentSong != null && !isNowPlayingExpanded && activeYouTubeVideo == null) {
                        MiniPlayerBar(
                            currentSong = currentSong!!,
                            isPlaying = isPlaying,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            visualizerBands = visualizerBands,
                            isMasterMuted = isMasterMuted,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNextClick = { viewModel.playNext() },
                            onMuteToggle = { viewModel.toggleMasterMute() },
                            onExpandClick = { isNowPlayingExpanded = true },
                            onCloseClick = { viewModel.stopLocalPlayback() }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = DjDeepSurface,
                        contentColor = DjTextPrimary,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 0.5.dp, color = DjBorderOutline)
                    ) {
                        Screen.bottomNavItems.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = DjAmberGold,
                                    indicatorColor = DjAmberGold,
                                    unselectedIconColor = DjTextSecondary,
                                    unselectedTextColor = DjTextTertiary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onCategoryClick = { category ->
                            viewModel.setSelectedCategoryFilter(category)
                            navController.navigate(Screen.Search.route) {
                                launchSingleTop = true
                            }
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        onLibraryClick = {
                            navController.navigate(Screen.Library.route)
                        },
                        onSettingsClick = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = viewModel,
                        onSearchYouTube = { query ->
                            if (query.isNotBlank()) {
                                viewModel.searchYouTube(query)
                            }
                            navController.navigate(Screen.YouTube.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.YouTube.route) {
                    YouTubeScreen(viewModel = viewModel)
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        viewModel = viewModel,
                        onCategoryClick = { category ->
                            viewModel.setSelectedCategoryFilter(category)
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }

                composable(Screen.Queue.route) {
                    QueueScreen(viewModel = viewModel)
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(viewModel = viewModel)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // Full Screen Now Playing Deck Overlay
        AnimatedVisibility(
            visible = isNowPlayingExpanded && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                viewModel = viewModel,
                onCollapse = { isNowPlayingExpanded = false }
            )
        }

        // Full Screen / Persistent Background Official YouTube Player
        if (activeYouTubeVideo != null) {
            Box(
                modifier = if (isYouTubeExpanded) {
                    Modifier.fillMaxSize().zIndex(100f)
                } else {
                    Modifier.size(1.dp).alpha(0f).zIndex(-1f)
                }
            ) {
                BackHandler(enabled = isYouTubeExpanded) {
                    viewModel.minimizeYouTubePlayer()
                }

                YouTubePlayerDetailView(
                    video = activeYouTubeVideo!!,
                    onClose = { viewModel.minimizeYouTubePlayer() },
                    onStop = { viewModel.stopYouTubePlayback() },
                    onSwitchTrack = { newVideo -> viewModel.playYouTubeVideo(newVideo) },
                    otherResults = youTubeSearchResults.filter { it.videoId != activeYouTubeVideo?.videoId },
                    isExpanded = isYouTubeExpanded
                )
            }
        }
    }
}
