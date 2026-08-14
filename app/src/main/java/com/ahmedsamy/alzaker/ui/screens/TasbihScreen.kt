package com.ahmedsamy.alzaker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.components.TasbihCounter
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors

/**
 * Tasbih tab mirroring the legacy app/(tabs)/tasbih.tsx: the theme gradient
 * with the standalone TasbihCounter (goal input shown, no fixed dhikr).
 */
@Composable
fun TasbihScreen(
    themeName: ThemeName,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    AppBackground(colors = themeColors(themeName), modifier = modifier) {
        TasbihCounter(showGoalInput = true, hapticsEnabled = hapticsEnabled)
    }
}
