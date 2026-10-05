package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CazielBlueColorScheme = darkColorScheme(
    primary = CyberBluePrimary,
    onPrimary = CyberBlueOnPrimary,
    primaryContainer = CyberBluePrimaryContainer,
    onPrimaryContainer = CyberBlueOnPrimaryContainer,
    secondary = CyberBlueSecondary,
    onSecondary = CyberBlueOnSecondary,
    secondaryContainer = CyberBlueSecondaryContainer,
    onSecondaryContainer = CyberBlueOnSecondaryContainer,
    tertiary = CyberBlueTertiary,
    onTertiary = CyberBlueOnTertiary,
    tertiaryContainer = CyberBlueTertiaryContainer,
    background = CyberBackground,
    onBackground = CyberOnBackground,
    surface = CyberSurface,
    onSurface = CyberOnSurface,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberOnSurfaceVariant,
    outline = CyberOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Always enforce the requested Blue Gaming Theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CazielBlueColorScheme,
        typography = Typography,
        content = content
    )
}
