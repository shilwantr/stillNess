package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.glass.LocalAmbientTheme
import com.example.ui.glass.isLight

@Composable
private fun getStillColorScheme() = if (LocalAmbientTheme.current.isLight) {
    lightColorScheme(
        primary = CyanGlow,
        onPrimary = Color.White,
        primaryContainer = GlassBackgroundBright,
        onPrimaryContainer = Color(0xFF1E2A20),
        secondary = VioletGlow,
        onSecondary = Color.White,
        secondaryContainer = GlassBackground,
        onSecondaryContainer = Color(0xFF1E2A20),
        tertiary = PinkGlow,
        background = Color(0xFFFCFDFC),
        onBackground = Color(0xFF1E2A20),
        surface = Color(0xFFF4F8F5),
        onSurface = Color(0xFF1E2A20),
        surfaceVariant = Color(0xFFEBF0EC),
        onSurfaceVariant = Color(0xFF3B503E)
    )
} else {
    darkColorScheme(
        primary = CyanGlow,
        onPrimary = Color.Black,
        primaryContainer = GlassBackgroundBright,
        onPrimaryContainer = Color(0xFFF0F4F2),
        secondary = VioletGlow,
        onSecondary = Color.Black,
        secondaryContainer = GlassBackground,
        onSecondaryContainer = Color(0xFFF0F4F2),
        tertiary = PinkGlow,
        background = MidnightDark,
        onBackground = Color(0xFFF0F4F2),
        surface = MidnightNavy,
        onSurface = Color(0xFFF0F4F2),
        surfaceVariant = MidnightSurface,
        onSurfaceVariant = Color(0xFFC2D1C9)
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getStillColorScheme(),
        typography = Typography,
        content = content
    )
}
