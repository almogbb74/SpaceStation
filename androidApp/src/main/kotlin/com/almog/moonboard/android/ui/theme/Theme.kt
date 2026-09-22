package com.almog.moonboard.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ponytail: hand-picked to match the reference screenshot by eye, not pixel-sampled.
val MoonBoardBackground = Color(0xFF1B2432)
val MoonBoardSurfaceVariant = Color(0xFF232D3E)
val MoonBoardAccent = Color(0xFFA192C5)
val MoonBoardTextPrimary = Color(0xFFE7E9F3)
val MoonBoardTextMuted = Color(0xFFAEB4C7)

private val MoonBoardColorScheme = darkColorScheme(
    primary = MoonBoardAccent,
    onPrimary = Color.White,
    secondary = MoonBoardAccent,
    background = MoonBoardBackground,
    onBackground = MoonBoardTextPrimary,
    surface = MoonBoardBackground,
    onSurface = MoonBoardTextPrimary,
    surfaceVariant = MoonBoardSurfaceVariant,
    onSurfaceVariant = MoonBoardTextMuted,
)

@Composable
fun MoonBoardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MoonBoardColorScheme, content = content)
}
