package com.fitworkup.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


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
/*
  val view = LocalView.current

  if (!view.isInEditMode) {
      SideEffect {
          val window = (view.context as Activity).window
          window.statusBarColor = colorScheme.background.toArgb()
          WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      }
  }
*/
  MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography, // Agora o compilador sabe exatamente qual é!
      content = content
  )
}