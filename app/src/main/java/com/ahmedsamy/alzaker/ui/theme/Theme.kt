package com.ahmedsamy.alzaker.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Application-wide font-size multiplier, mirroring the legacy FontSizeContext
 * (font_size_multiplier). Screen composables scale their base font sizes by
 * this value, exactly like the Expo screens multiply every fontSize.
 */
val LocalFontSizeMultiplier = staticCompositionLocalOf { 1f }

/**
 * Root theme composable. Defaults to the dark palettes (the legacy app always
 * draws white text over a dark gradient, so its look is inherently dark) and
 * to dynamic color OFF so the five brand gradients stay intact. Dynamic color
 * can be enabled per-call on Android 12+.
 */
@Composable
fun AlzakerTheme(
    themeName: ThemeName = ThemeName.DEFAULT,
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    fontSizeMultiplier: Float = 1f,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> darkSchemes[themeName] ?: darkSchemes.getValue(ThemeName.DEFAULT)

        else -> lightSchemes[themeName] ?: lightSchemes.getValue(ThemeName.DEFAULT)
    }

    val defaultTypography = MaterialTheme.typography
    CompositionLocalProvider(LocalFontSizeMultiplier provides fontSizeMultiplier) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typographyWithAmiri(defaultTypography),
            content = content,
        )
    }
}

/**
 * Dark scheme: the faithful legacy look. The brand gradient anchors
 * background/surface, white is the text color, and the legacy white pill
 * button (rgba(255,255,255,0.95) with primary-colored text) maps onto
 * primaryContainer = white / onPrimaryContainer = primary.
 */
private fun darkScheme(theme: AlzakerThemeColors): ColorScheme = darkColorScheme(
    primary = theme.primary,
    onPrimary = theme.text,
    primaryContainer = LegacyColors.White,
    onPrimaryContainer = theme.primary,
    secondary = theme.accent,
    onSecondary = LegacyColors.Black,
    tertiary = theme.primaryLight,
    onTertiary = theme.text,
    background = theme.primary,
    onBackground = theme.text,
    surface = theme.primary,
    onSurface = theme.text,
    surfaceVariant = theme.primaryLight,
    onSurfaceVariant = LegacyColors.White.copy(alpha = 0.8f),
    error = LegacyColors.Red,
    onError = LegacyColors.White,
    outline = LegacyColors.White.copy(alpha = 0.5f),
)

/**
 * Light scheme: same brand gradient identity, but with white surfaces and
 * primary-colored content so system-light users get readable components.
 */
private fun lightScheme(theme: AlzakerThemeColors): ColorScheme = lightColorScheme(
    primary = theme.primary,
    onPrimary = theme.text,
    primaryContainer = LegacyColors.White,
    onPrimaryContainer = theme.primary,
    secondary = theme.accent,
    onSecondary = LegacyColors.Black,
    tertiary = theme.primaryLight,
    onTertiary = theme.text,
    background = theme.primaryLight,
    onBackground = theme.text,
    surface = LegacyColors.White,
    onSurface = theme.primary,
    surfaceVariant = theme.primaryLight,
    onSurfaceVariant = LegacyColors.White.copy(alpha = 0.8f),
    error = LegacyColors.Red,
    onError = LegacyColors.White,
    outline = LegacyColors.White.copy(alpha = 0.5f),
)

private val darkSchemes: Map<ThemeName, ColorScheme> =
    AlzakerThemes.mapValues { (_, theme) -> darkScheme(theme) }

private val lightSchemes: Map<ThemeName, ColorScheme> =
    AlzakerThemes.mapValues { (_, theme) -> lightScheme(theme) }
