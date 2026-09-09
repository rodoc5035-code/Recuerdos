package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandVioletLight,
    onPrimary = Color(0xFF100735),
    primaryContainer = Color(0xFF29225E),
    onPrimaryContainer = Color(0xFFEEEAFF),
    secondary = BrandPink,
    onSecondary = Color(0xFF3B0017),
    secondaryContainer = Color(0xFF54122D),
    onSecondaryContainer = BrandPinkLight,
    tertiary = BrandBlue,
    onTertiary = MinimalWhiteSurface,
    background = MinimalBlackBg,
    onBackground = MinimalLightText,
    surface = MinimalBlackSurface,
    onSurface = MinimalLightText,
    surfaceVariant = MinimalBlackSurfaceVariant,
    onSurfaceVariant = MinimalLightSecondaryText,
    outline = MinimalDarkOutline,
    outlineVariant = MinimalDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = BrandViolet,
    onPrimary = MinimalWhiteSurface,
    primaryContainer = Color(0xFFEFEBFF),
    onPrimaryContainer = BrandVioletDark,
    secondary = BrandPink,
    onSecondary = MinimalWhiteSurface,
    secondaryContainer = BrandPinkContainer,
    onSecondaryContainer = BrandPink,
    tertiary = BrandBlue,
    onTertiary = MinimalWhiteSurface,
    background = MinimalWhiteBg,
    onBackground = MinimalDarkText,
    surface = MinimalWhiteSurface,
    onSurface = MinimalDarkText,
    surfaceVariant = MinimalWhiteSurfaceVariant,
    onSurfaceVariant = MinimalDarkSecondaryText,
    outline = MinimalLightOutline,
    outlineVariant = MinimalLightOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

