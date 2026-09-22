package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DjAmberGold,
    onPrimary = DjObsidianBlack,
    primaryContainer = DjHighlightCard,
    onPrimaryContainer = DjAmberGold,
    secondary = DjElectricCyan,
    onSecondary = DjObsidianBlack,
    secondaryContainer = DjElevatedCard,
    onSecondaryContainer = DjElectricCyan,
    tertiary = DjNeonEmerald,
    onTertiary = DjObsidianBlack,
    background = DjObsidianBlack,
    onBackground = DjTextPrimary,
    surface = DjDeepSurface,
    onSurface = DjTextPrimary,
    surfaceVariant = DjElevatedCard,
    onSurfaceVariant = DjTextSecondary,
    outline = DjBorderOutline,
    error = DjCrimsonCue,
    onError = DjObsidianBlack
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
