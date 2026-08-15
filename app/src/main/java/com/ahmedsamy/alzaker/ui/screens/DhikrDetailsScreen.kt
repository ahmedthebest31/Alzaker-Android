package com.ahmedsamy.alzaker.ui.screens

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.components.TasbihCounter
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors

/**
 * Dhikr counter route mirroring the legacy app/dhikr-details.tsx: the theme
 * gradient with a fixed TasbihCounter (dhikr text + required repeat count,
 * no goal input). Opened from the favorites and adhkar lists (legacy
 * DhikrCard tap).
 */
@Composable
fun DhikrDetailsScreen(
    dhikr: String,
    repeat: Int,
    themeName: ThemeName,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    AppBackground(colors = themeColors(themeName), modifier = modifier) {
        TasbihCounter(
            initialDhikrText = dhikr,
            initialRepeatCount = repeat,
            showGoalInput = false,
            hapticsEnabled = hapticsEnabled,
            modifier = Modifier.statusBarsPadding(),
        )
    }
}
