package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = SpaceBlack,
    primaryContainer = SlateCardDark,
    onPrimaryContainer = CyberCyan,
    secondary = NeonViolet,
    onSecondary = SpaceBlack,
    secondaryContainer = SlateCardDark,
    onSecondaryContainer = NeonViolet,
    tertiary = AmbientMint,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextPrimaryDark,
    surface = ObsidianDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateBorderDark,
    error = DangerAmber,
    onError = SpaceBlack
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = DeepViolet,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF5B21B6),
    tertiary = Color(0xFF059669),
    onTertiary = PureWhite,
    background = SoftCanvasLight,
    onBackground = TextPrimaryLight,
    surface = CardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = DangerAmber,
    onError = PureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Defaulting to DarkColorScheme gives that authentic JARVIS / Voice AI aesthetic
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
