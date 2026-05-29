package com.perpetuitylab.livenews.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = LiveNewsAccent,
    onPrimary = LiveNewsTextPrimary,
    primaryContainer = LiveNewsAccentSoft,
    onPrimaryContainer = LiveNewsTextPrimary,
    secondary = LiveNewsInfo,
    onSecondary = LiveNewsBackground,
    tertiary = LiveNewsTextSecondary,
    background = LiveNewsBackground,
    onBackground = LiveNewsTextPrimary,
    surface = LiveNewsSurface,
    onSurface = LiveNewsTextPrimary,
    surfaceVariant = LiveNewsSurfaceStrong,
    onSurfaceVariant = LiveNewsTextSecondary,
    outline = LiveNewsBorder,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LiveNewsAccent,
    onPrimary = LiveNewsTextPrimary,
    primaryContainer = LiveNewsAccentSoft,
    onPrimaryContainer = LiveNewsTextPrimary,
    secondary = LiveNewsInfo,
    onSecondary = LiveNewsBackground,
    tertiary = LiveNewsTextSecondary,
    background = LiveNewsBackground,
    onBackground = LiveNewsTextPrimary,
    surface = LiveNewsSurface,
    onSurface = LiveNewsTextPrimary,
    surfaceVariant = LiveNewsSurfaceStrong,
    onSurfaceVariant = LiveNewsTextSecondary,
    outline = LiveNewsBorder,
  )

@Composable
fun LiveNewsTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  @Suppress("UNUSED_VARIABLE")
  val dynamicColorDisabled = dynamicColor
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
