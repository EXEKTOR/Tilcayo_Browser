package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TilcayoGold,
    onPrimary = Color(0xFF281500),
    primaryContainer = TilcayoGoldDark,
    onPrimaryContainer = Color(0xFFFFDDB8),
    secondary = TilcayoAiBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E294B),
    onSecondaryContainer = Color(0xFFD6E2FF),
    tertiary = TilcayoAiPurple,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = TilcayoGold,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE8D1),
    onPrimaryContainer = TilcayoGoldDark,
    secondary = TilcayoAiBlue,
    onSecondary = Color.White,
    secondaryContainer = TilcayoAiPillBg,
    onSecondaryContainer = Color(0xFF1E294B),
    tertiary = TilcayoAiPurple,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF191C20),
    surface = Color.White,
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFEBEFF5),
    onSurfaceVariant = Color(0xFF495057),
    outline = Color(0xFFD0D5DD)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded Tilcayo colors consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
