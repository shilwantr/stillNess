package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StillDarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = Color.Black,
    primaryContainer = GlassBackgroundBright,
    onPrimaryContainer = TextWhite,
    secondary = VioletGlow,
    onSecondary = Color.Black,
    secondaryContainer = GlassBackground,
    onSecondaryContainer = TextWhite,
    tertiary = PinkGlow,
    background = MidnightDark,
    onBackground = TextWhite,
    surface = MidnightNavy,
    onSurface = TextWhite,
    surfaceVariant = MidnightSurface,
    onSurfaceVariant = TextMuted
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StillDarkColorScheme,
        typography = Typography,
        content = content
    )
}
