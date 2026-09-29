package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Data structure representing a fully customizable DJ stage visual theme.
 */
data class DjThemeColors(
    val id: String,
    val name: String,
    val subtitle: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceHighlight: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val error: Color,
    val accentGlow: Color,
    val gradientBrush: Brush,
    val cardGradient: Brush
)

object DjThemes {
    const val DEFAULT_THEME_ID = "obsidian_gold"
    const val THEME_CYBERPUNK = "cyberpunk_neon"
    const val THEME_MATRIX = "matrix_emerald"
    const val THEME_SUNSET = "sunset_flame"
    const val THEME_SAPPHIRE = "midnight_sapphire"

    val ObsidianGold = DjThemeColors(
        id = DEFAULT_THEME_ID,
        name = "Club Obsidian Gold",
        subtitle = "Flagship VIP DJ deck with amber gold & electric cyan accents",
        primary = Color(0xFFFFB300),
        secondary = Color(0xFF00E5FF),
        tertiary = Color(0xFF00E676),
        background = Color(0xFF090A0F),
        surface = Color(0xFF12151F),
        surfaceElevated = Color(0xFF1B202D),
        surfaceHighlight = Color(0xFF252C3D),
        border = Color(0xFF2C3246),
        textPrimary = Color(0xFFF6F7FA),
        textSecondary = Color(0xFF9EA3B8),
        textTertiary = Color(0xFF656A82),
        error = Color(0xFFFF5252),
        accentGlow = Color(0x33FFB300),
        gradientBrush = Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF8F00))),
        cardGradient = Brush.verticalGradient(listOf(Color(0xFF1B202D), Color(0xFF131722)))
    )

    val CyberpunkNeon = DjThemeColors(
        id = THEME_CYBERPUNK,
        name = "Cyberpunk Neon",
        subtitle = "Futuristic nightclub atmosphere with glowing magenta & vivid cyan",
        primary = Color(0xFFFF007F),
        secondary = Color(0xFF00F0FF),
        tertiary = Color(0xFFB026FF),
        background = Color(0xFF0B0814),
        surface = Color(0xFF140F24),
        surfaceElevated = Color(0xFF1E1735),
        surfaceHighlight = Color(0xFF2B204B),
        border = Color(0xFF3E2D68),
        textPrimary = Color(0xFFFBF7FF),
        textSecondary = Color(0xFFBCAFD4),
        textTertiary = Color(0xFF7D7299),
        error = Color(0xFFFF3366),
        accentGlow = Color(0x44FF007F),
        gradientBrush = Brush.horizontalGradient(listOf(Color(0xFFFF007F), Color(0xFFB026FF))),
        cardGradient = Brush.verticalGradient(listOf(Color(0xFF1E1735), Color(0xFF140F24)))
    )

    val MatrixEmerald = DjThemeColors(
        id = THEME_MATRIX,
        name = "Matrix Stage Emerald",
        subtitle = "Concert laser lighting with acid neon green & mint luminescence",
        primary = Color(0xFF00FF87),
        secondary = Color(0xFF60EFFF),
        tertiary = Color(0xFF00E5A3),
        background = Color(0xFF050E0A),
        surface = Color(0xFF0B1A13),
        surfaceElevated = Color(0xFF132A1F),
        surfaceHighlight = Color(0xFF1D3C2D),
        border = Color(0xFF214A36),
        textPrimary = Color(0xFFF0FFF7),
        textSecondary = Color(0xFFA5C9B7),
        textTertiary = Color(0xFF668F7A),
        error = Color(0xFFFF4D4D),
        accentGlow = Color(0x3300FF87),
        gradientBrush = Brush.horizontalGradient(listOf(Color(0xFF00FF87), Color(0xFF00E5A3))),
        cardGradient = Brush.verticalGradient(listOf(Color(0xFF132A1F), Color(0xFF0B1A13)))
    )

    val SunsetFlame = DjThemeColors(
        id = THEME_SUNSET,
        name = "Sunset Festival Flame",
        subtitle = "High-energy festival mainstage with fiery solar orange & golden amber",
        primary = Color(0xFFFF5722),
        secondary = Color(0xFFFFB300),
        tertiary = Color(0xFFFF1744),
        background = Color(0xFF110808),
        surface = Color(0xFF1D1010),
        surfaceElevated = Color(0xFF2B1717),
        surfaceHighlight = Color(0xFF3D2020),
        border = Color(0xFF522828),
        textPrimary = Color(0xFFFFF7F5),
        textSecondary = Color(0xFFD4B0AB),
        textTertiary = Color(0xFF94726D),
        error = Color(0xFFFF3D00),
        accentGlow = Color(0x33FF5722),
        gradientBrush = Brush.horizontalGradient(listOf(Color(0xFFFF5722), Color(0xFFFF9800))),
        cardGradient = Brush.verticalGradient(listOf(Color(0xFF2B1717), Color(0xFF1D1010)))
    )

    val MidnightSapphire = DjThemeColors(
        id = THEME_SAPPHIRE,
        name = "Midnight Arena Sapphire",
        subtitle = "Stadium arena with royal ice sapphire blue & electric azure rays",
        primary = Color(0xFF2979FF),
        secondary = Color(0xFF00E5FF),
        tertiary = Color(0xFF7C4DFF),
        background = Color(0xFF060A17),
        surface = Color(0xFF0D1429),
        surfaceElevated = Color(0xFF152040),
        surfaceHighlight = Color(0xFF1E2D58),
        border = Color(0xFF293C72),
        textPrimary = Color(0xFFF3F7FF),
        textSecondary = Color(0xFFA8B7DE),
        textTertiary = Color(0xFF6C7C9E),
        error = Color(0xFFFF5252),
        accentGlow = Color(0x332979FF),
        gradientBrush = Brush.horizontalGradient(listOf(Color(0xFF2979FF), Color(0xFF00E5FF))),
        cardGradient = Brush.verticalGradient(listOf(Color(0xFF152040), Color(0xFF0D1429)))
    )

    val allThemes: List<DjThemeColors> = listOf(
        ObsidianGold,
        CyberpunkNeon,
        MatrixEmerald,
        SunsetFlame,
        MidnightSapphire
    )

    fun getTheme(id: String): DjThemeColors {
        return allThemes.find { it.id == id } ?: ObsidianGold
    }
}

val LocalDjColors = staticCompositionLocalOf { DjThemes.ObsidianGold }

val MaterialTheme.djColors: DjThemeColors
    @Composable
    @ReadOnlyComposable
    get() = LocalDjColors.current

@Composable
fun MyApplicationTheme(
    themeId: String = DjThemes.DEFAULT_THEME_ID,
    content: @Composable () -> Unit,
) {
    val djTheme = DjThemes.getTheme(themeId)

    val m3ColorScheme = darkColorScheme(
        primary = djTheme.primary,
        onPrimary = Color.Black,
        primaryContainer = djTheme.surfaceHighlight,
        onPrimaryContainer = djTheme.primary,
        secondary = djTheme.secondary,
        onSecondary = Color.Black,
        secondaryContainer = djTheme.surfaceElevated,
        onSecondaryContainer = djTheme.secondary,
        tertiary = djTheme.tertiary,
        onTertiary = Color.Black,
        background = djTheme.background,
        onBackground = djTheme.textPrimary,
        surface = djTheme.surface,
        onSurface = djTheme.textPrimary,
        surfaceVariant = djTheme.surfaceElevated,
        onSurfaceVariant = djTheme.textSecondary,
        outline = djTheme.border,
        error = djTheme.error,
        onError = Color.Black
    )

    CompositionLocalProvider(LocalDjColors provides djTheme) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
