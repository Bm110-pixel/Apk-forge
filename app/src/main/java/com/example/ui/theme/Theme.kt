package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekLightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekOnPrimaryContainer,
    secondary = SleekSecondary,
    onSecondary = Color.White,
    secondaryContainer = SleekSecondaryContainer,
    onSecondaryContainer = SleekOnSecondaryContainer,
    tertiary = SleekTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SleekTertiaryContainer,
    onTertiaryContainer = SleekOnTertiaryContainer,
    background = SleekBackground,
    onBackground = SleekTextPrimary,
    surface = SleekSurface,
    onSurface = SleekTextPrimary,
    surfaceVariant = SleekSurfaceContainer,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekCardBorder,
    outlineVariant = SleekCardBorderSubtle,
    error = SleekError,
    onError = Color.White,
    errorContainer = SleekErrorContainer,
    onErrorContainer = Color(0xFF410002)
)

private val SleekDarkColorScheme = darkColorScheme(
    primary = SleekPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SleekPrimaryDark,
    onPrimaryContainer = SleekPrimaryContainer,
    secondary = SleekSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3F4759),
    onSecondaryContainer = SleekSecondaryContainer,
    tertiary = Color(0xFFDFBDE5),
    onTertiary = Color(0xFF402843),
    tertiaryContainer = SleekTertiary,
    onTertiaryContainer = SleekTertiaryContainer,
    background = Color(0xFF121318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF1B1B22),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF262832),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to Sleek Interface light aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SleekDarkColorScheme else SleekLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

