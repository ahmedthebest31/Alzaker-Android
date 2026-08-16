package com.ahmedsamy.alzaker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The five selectable app themes, mirroring the legacy Expo app keys in
 * utils/colors.ts. Enum values intentionally use the exact persisted strings
 * so AppSettings.themeName maps directly onto [fromKey].
 */
enum class ThemeName(val key: String) {
    DEFAULT("default"),
    MIDNIGHT("midnight"),
    NATURE("nature"),
    ROYAL("royal"),
    HIGH_CONTRAST("highContrast");

    companion object {
        fun fromKey(key: String): ThemeName =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

/**
 * Brand colors of one theme, mirroring the legacy Theme interface
 * (primary, primaryLight, optional accent, optional text). The legacy app
 * paints every screen with a vertical gradient from [primary] (top) to
 * [primaryLight] (bottom), white text on top, gold accents.
 */
data class AlzakerThemeColors(
    val primary: Color,
    val primaryLight: Color,
    val accent: Color = Gold,
    val text: Color = Color.White,
)

/** Gold accent shared by every theme (legacy colors.gold). */
val Gold = Color(0xFFFFD700)

/**
 * The five themes. Hex values were redesigned on 2026-08-17 into a dark,
 * gold-accented Islamic palette: every gradient stop keeps WCAG AAA contrast
 * (>= 7:1) with the white text painted on top, and the gold accent stays
 * readable on both stops (>= 4.5:1). The persisted theme keys are unchanged.
 */
val AlzakerThemes: Map<ThemeName, AlzakerThemeColors> = mapOf(
    ThemeName.DEFAULT to AlzakerThemeColors(
        primary = Color(0xFF0B3D2B),
        primaryLight = Color(0xFF155E46),
    ),
    ThemeName.MIDNIGHT to AlzakerThemeColors(
        primary = Color(0xFF0C1126),
        primaryLight = Color(0xFF1F2A5A),
    ),
    ThemeName.NATURE to AlzakerThemeColors(
        primary = Color(0xFF05333A),
        primaryLight = Color(0xFF0D555B),
    ),
    ThemeName.ROYAL to AlzakerThemeColors(
        primary = Color(0xFF241040),
        primaryLight = Color(0xFF422765),
    ),
    ThemeName.HIGH_CONTRAST to AlzakerThemeColors(
        primary = Color.Black,
        primaryLight = Color.Black,
        accent = Color(0xFFFFFF00),
        text = Color.White,
    ),
)

fun themeColors(themeName: ThemeName): AlzakerThemeColors =
    AlzakerThemes[themeName] ?: AlzakerThemes.getValue(ThemeName.DEFAULT)

/** Shared legacy palette from utils/colors.ts (`colors` object). */
object LegacyColors {
    val White = Color.White
    val Black = Color.Black
    val Gray = Color(0xFF333333)
    val LightGray = Color(0xFFEEEEEE)
    val MediumGray = Color(0xFF767577)
    val LightBlue = Color(0xFF81B0FF)
    val OffWhite = Color(0xFFF4F3F4)
    val Gold = Color(0xFFFFD700)
    val Red = Color(0xFFFF5252)
}
