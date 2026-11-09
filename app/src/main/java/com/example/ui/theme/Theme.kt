package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SmartColorScheme = darkColorScheme(
    primary = SmartPrimary,
    onPrimary = Color(0xFF101118),
    primaryContainer = SmartPillActive,
    onPrimaryContainer = Color(0xFFD6E2FF),
    secondary = SmartPurple,
    onSecondary = Color(0xFF101118),
    tertiary = SmartSuccess,
    onTertiary = Color(0xFF0F2918),
    background = SmartDarkBg,
    onBackground = SmartText,
    surface = SmartSurface,
    onSurface = SmartText,
    surfaceVariant = SmartSurfaceElevated,
    onSurfaceVariant = SmartTextMuted,
    outline = SmartBorder,
    error = SmartError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartColorScheme,
        typography = Typography,
        content = content
    )
}
