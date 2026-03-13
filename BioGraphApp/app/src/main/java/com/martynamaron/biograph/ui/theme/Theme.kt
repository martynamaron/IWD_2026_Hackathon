package com.martynamaron.biograph.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenMid,
    onPrimary = Color.White,
    primaryContainer = GreenLightest,
    onPrimaryContainer = GreenDarkest,
    secondary = GreenDarkest,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = GreenDarkest,
    tertiary = GreenLightest,
    onTertiary = GreenDarkest,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = GreenDarkest,
    background = Color.White,
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = GrayBackground,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}