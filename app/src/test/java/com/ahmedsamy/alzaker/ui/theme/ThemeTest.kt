package com.ahmedsamy.alzaker.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun defaultThemeUsesEmeraldPalette() {
        val theme = AlzakerThemes.getValue(ThemeName.DEFAULT)
        assertEquals(Color(0xFF0B3D2B), theme.primary)
        assertEquals(Color(0xFF155E46), theme.primaryLight)
    }

    @Test
    fun highContrastThemeMatchesLegacyValues() {
        val theme = AlzakerThemes.getValue(ThemeName.HIGH_CONTRAST)
        assertEquals(Color.Black, theme.primary)
        assertEquals(Color(0xFFFFFF00), theme.accent)
        assertEquals(Color.White, theme.text)
    }

    @Test
    fun allGradientStopsKeepAaaWhiteTextContrast() {
        AlzakerThemes.forEach { (name, theme) ->
            assertTrue("$name primary vs white < 7:1", contrast(theme.primary, Color.White) >= 7.0)
            assertTrue("$name primaryLight vs white < 7:1", contrast(theme.primaryLight, Color.White) >= 7.0)
        }
    }

    @Test
    fun accentStaysReadableOnEveryGradientStop() {
        val dbg = AlzakerThemes.getValue(ThemeName.DEFAULT)
        assertEquals("DEFAULT accent must be the Gold constant", Gold, dbg.accent)
        AlzakerThemes.forEach { (name, theme) ->
            val r = contrast(theme.accent, theme.primary)
            assertTrue("$name accent vs primary = $r (need >= 4.5) red=${theme.accent.red} green=${theme.accent.green} blue=${theme.accent.blue}", r >= 4.5)
            val r2 = contrast(theme.accent, theme.primaryLight)
            assertTrue("$name accent vs primaryLight = $r2 (need >= 4.5)", r2 >= 4.5)
        }
    }

    private fun linear(c: Float): Double =
        if (c <= 0.03928) (c / 12.92).toDouble() else Math.pow(((c + 0.055) / 1.055).toDouble(), 2.4)

    private fun luminance(color: Color): Double =
        0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)

    private fun contrast(a: Color, b: Color): Double {
        val l1 = luminance(a)
        val l2 = luminance(b)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }
}
