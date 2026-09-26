@file:Suppress("FunctionName")

package com.almog.moonboard.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ponytail: hand-picked to match the reference screenshot by eye, not pixel-sampled.
val MoonBoardBackground = Color(0xFF1B2432)
val MoonBoardSurfaceVariant = Color(0xFF232D3E)
val MoonBoardAccent = Color(0xFFA192C5)
val MoonBoardTextPrimary = Color(0xFFE7E9F3)
val MoonBoardTextMuted = Color(0xFFAEB4C7)
val MoonBoardSuccess = Color(0xFF6FCF97)

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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ColorPalettePreview() {
    val swatches = listOf(
        "Background" to MoonBoardBackground,
        "SurfaceVariant" to MoonBoardSurfaceVariant,
        "Accent" to MoonBoardAccent,
        "TextPrimary" to MoonBoardTextPrimary,
        "TextMuted" to MoonBoardTextMuted,
        "Success" to MoonBoardSuccess,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(12.dp)) {
        swatches.forEach { (name, color) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(48.dp).background(color))
                Text(name, color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
