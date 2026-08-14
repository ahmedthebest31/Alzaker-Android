package com.ahmedsamy.alzaker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.ahmedsamy.alzaker.R

/**
 * Amiri typeface, mirroring the legacy expo-font registration of
 * 'Amiri-Regular' that the Expo app applies to every text style.
 */
val AmiriFontFamily = FontFamily(Font(R.font.amiri_regular))

/**
 * Returns [typography] with every standard style re-typed in the Amiri
 * family. The Material3 Typography constructor is internal in current
 * material3 versions, so the framework default is transformed through the
 * public `copy` API instead of rebuilt from scratch.
 */
fun typographyWithAmiri(typography: Typography): Typography = typography.copy(
    displayLarge = typography.displayLarge.copy(fontFamily = AmiriFontFamily),
    displayMedium = typography.displayMedium.copy(fontFamily = AmiriFontFamily),
    displaySmall = typography.displaySmall.copy(fontFamily = AmiriFontFamily),
    headlineLarge = typography.headlineLarge.copy(fontFamily = AmiriFontFamily),
    headlineMedium = typography.headlineMedium.copy(fontFamily = AmiriFontFamily),
    headlineSmall = typography.headlineSmall.copy(fontFamily = AmiriFontFamily),
    titleLarge = typography.titleLarge.copy(fontFamily = AmiriFontFamily),
    titleMedium = typography.titleMedium.copy(fontFamily = AmiriFontFamily),
    titleSmall = typography.titleSmall.copy(fontFamily = AmiriFontFamily),
    bodyLarge = typography.bodyLarge.copy(fontFamily = AmiriFontFamily),
    bodyMedium = typography.bodyMedium.copy(fontFamily = AmiriFontFamily),
    bodySmall = typography.bodySmall.copy(fontFamily = AmiriFontFamily),
    labelLarge = typography.labelLarge.copy(fontFamily = AmiriFontFamily),
    labelMedium = typography.labelMedium.copy(fontFamily = AmiriFontFamily),
    labelSmall = typography.labelSmall.copy(fontFamily = AmiriFontFamily),
)
