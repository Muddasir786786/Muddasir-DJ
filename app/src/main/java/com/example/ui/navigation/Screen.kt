package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Live Deck", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object YouTube : Screen("youtube", "YouTube", Icons.Default.SmartDisplay)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Queue : Screen("queue", "Queue", Icons.Default.QueueMusic)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object NowPlaying : Screen("now_playing", "Now Playing", Icons.Default.LibraryMusic)

    companion object {
        val bottomNavItems = listOf(
            Home,
            Search,
            YouTube,
            Library,
            Queue,
            Favorites,
            Settings
        )
    }
}
