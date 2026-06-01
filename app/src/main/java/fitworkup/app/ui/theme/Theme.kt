package com.fitworkup.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = FitRed,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = BackgroundLight,
    onBackground = OnBackgroundLight,
)

private val DarkColors = darkColorScheme(
    primary = FitRedDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnBackgroundDark,
    onBackground = OnBackgroundDark,
)

@Composable
fun FitWorkUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}