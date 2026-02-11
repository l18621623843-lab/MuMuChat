package com.kk.mumuchat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = SkyBlueDark,
    secondary = SkyBlueDark,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    outline = DividerColor,
    error = UnreadBadge,
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A3050),
    onPrimaryContainer = SkyBlueLight,
    secondary = SkyBlue,
    onSecondary = Color.White,
    background = Color(0xFF0E1B2D),
    onBackground = Color.White,
    surface = Color(0xFF142233),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A3050),
    outline = Color.White.copy(alpha = 0.12f),
    error = UnreadBadge,
)

@Composable
fun MuMuChatTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val mumuColors = if (isDarkTheme) DarkMuMuColors else LightMuMuColors
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalMuMuColors provides mumuColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
