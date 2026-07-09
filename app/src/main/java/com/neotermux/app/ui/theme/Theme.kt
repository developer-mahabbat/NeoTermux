package com.neotermux.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = Color(0xFF5F6368),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EAED),
    onSecondaryContainer = Color(0xFF1D1B20),
    tertiary = Color(0xFF188038),
    onTertiary = Color.White,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = Color(0xFFD93025),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF9AA0A6),
    onSecondary = Color(0xFF2C2C2E),
    secondaryContainer = Color(0xFF3C4043),
    onSecondaryContainer = Color(0xFFE8EAED),
    tertiary = Color(0xFF81C995),
    onTertiary = Color(0xFF00391B),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

data class ThemeMode(val isDark: Boolean = false, val isDynamic: Boolean = true)

@Composable
fun NeoTermuxTheme(
    themeMode: ThemeMode = ThemeMode(),
    content: @Composable () -> Unit
) {
    val isDark = when {
        themeMode.isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            isSystemInDarkTheme()
        }
        else -> themeMode.isDark
    }
    val colorScheme = if (isDark) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
