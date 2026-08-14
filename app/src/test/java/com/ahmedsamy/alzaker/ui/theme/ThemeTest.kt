package com.ahmedsamy.alzaker.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {

    @Test
    fun everyLegacyThemeKeyResolvesToItsTheme() {
        assertEquals(ThemeName.DEFAULT, ThemeName.fromKey("default"))
        assertEquals(ThemeName.MIDNIGHT, ThemeName.fromKey("midnight"))
        assertEquals(ThemeName.NATURE, ThemeName.fromKey("nature"))
        assertEquals(ThemeName.ROYAL, ThemeName.fromKey("royal"))
        assertEquals(ThemeName.HIGH_CONTRAST, ThemeName.fromKey("highContrast"))
    }

    @Test
    fun unknownThemeKeyFallsBackToDefault() {
        assertEquals(ThemeName.DEFAULT, ThemeName.fromKey("unknown"))
        assertEquals(ThemeName.DEFAULT, ThemeName.fromKey(""))
    }

    @Test
    fun allFiveLegacyThemesAreDefined() {
        assertEquals(5, ThemeName.entries.size)
        assertEquals(ThemeName.entries.size, AlzakerThemes.size)
    }

    @Test
    fun defaultThemeMatchesLegacyHexValues() {
        val theme = AlzakerThemes.getValue(ThemeName.DEFAULT)
        assertEquals(Color(0xFF1E3C72), theme.primary)
        assertEquals(Color(0xFF2A5298), theme.primaryLight)
    }

    @Test
    fun highContrastThemeMatchesLegacyValues() {
        val theme = AlzakerThemes.getValue(ThemeName.HIGH_CONTRAST)
        assertEquals(Color.Black, theme.primary)
        assertEquals(Color(0xFFFFFF00), theme.accent)
        assertEquals(Color.White, theme.text)
    }
}
