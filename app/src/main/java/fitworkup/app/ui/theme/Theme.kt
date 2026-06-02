package com.fitworkup.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// SOLUÇÃO: Força o compilador a usar a sua Typography do Material 3, ignorando a do Kotlin
import com.fitworkup.app.ui.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = BrandRed,
    background = DarkBackground,
    surface = DarkCardSurface,
    onPrimary = DarkTextPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    error = SystemWarningAmber
)

private val LightColorScheme = lightColorScheme(
    primary = BrandRed,
    background = LightBackground,
    surface = LightCardSurface,
    onPrimary = LightBackground,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    error = SystemWarningAmber
)

@Composable
fun FitWorkUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Agora o compilador sabe exatamente qual é!
        content = content
    )
}