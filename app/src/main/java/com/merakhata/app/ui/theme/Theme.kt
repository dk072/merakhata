package com.merakhata.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerNavy,
    onPrimaryContainer = Color.White,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerMint,
    onSecondaryContainer = OnSecondaryContainerTeal,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerPink,
    onErrorContainer = OnErrorContainerRed,
    tertiary = WarmGold,
    background = SurfaceBg,
    onBackground = OnSurfaceDark,
    surface = SurfaceContainerLowest,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = OnSurfaceVariantGray,
    outline = OutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    secondary = DeepEmerald,
    tertiary = WarmGold,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0xFF334155),
    surfaceVariant = Color(0xFF334155)
)

@Composable
fun MeraKhataTheme(
    darkTheme: Boolean = false, // Default to crisp light theme for maximum readability
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = HeaderGradientStart.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
