package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AlphaDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianDark,
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = Color(0xFF99F6FF),
    
    secondary = NeonEmerald,
    onSecondary = ObsidianDark,
    secondaryContainer = Color(0xFF00391A),
    onSecondaryContainer = Color(0xFF70FF9D),
    
    tertiary = ElectricViolet,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3B1E78),
    onTertiaryContainer = Color(0xFFE9DDFF),
    
    background = ObsidianDark,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorderGlow,
    
    error = CrimsonAlert,
    onError = Color.White
)

private val AlphaLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF047857),
    
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF6D28D9),
    
    background = AlphaLightBg,
    onBackground = TextPrimaryLight,
    surface = AlphaLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = AlphaLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = AlphaLightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun AlphaVpnTheme(
    darkTheme: Boolean = true, // Default to sleek cyber dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AlphaDarkColorScheme else AlphaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
