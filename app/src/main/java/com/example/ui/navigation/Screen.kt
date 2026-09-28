package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // 4 Primary Bottom Navigation Destinations
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object More : Screen("more", "More", Icons.Default.MoreHoriz)

    // Secondary / Sub-screen Destinations
    object CategoryDetail : Screen("category/{slug}", "Category", Icons.Default.Category) {
        fun createRoute(slug: String) = "category/$slug"
    }
    object Queue : Screen("queue", "Queue", Icons.Default.QueueMusic)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object YouTube : Screen("youtube", "YouTube", Icons.Default.SmartDisplay)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object NowPlaying : Screen("now_playing", "Now Playing", Icons.Default.LibraryMusic)

    companion object {
        val bottomNavItems = listOf(
            Home,
            Search,
            Library,
            More
        )
    }
}
