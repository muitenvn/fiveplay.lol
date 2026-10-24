package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FivePlayDarkScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = Color(0xFF00201B),
    primaryContainer = Color(0xFF004D42),
    onPrimaryContainer = NeonEmerald,
    secondary = LosSantosGold,
    onSecondary = Color(0xFF281B00),
    secondaryContainer = Color(0xFF3E2C00),
    onSecondaryContainer = LosSantosGold,
    tertiary = ViceMagenta,
    onTertiary = Color.White,
    background = CyberBackground,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FivePlayDarkScheme,
        typography = Typography,
        content = content
    )
}
