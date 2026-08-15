package com.ahmedsamy.alzaker.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ahmedsamy.alzaker.data.local.TasbihStore
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.components.TasbihCounter
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors

/**
 * Tasbih tab mirroring the legacy app/(tabs)/tasbih.tsx: the theme gradient
 * with the standalone TasbihCounter (goal input shown, no fixed dhikr). The
 * count and goal are bound to [TasbihStore] so the home-screen tasbih widget
 * and this tab always show the same values; preference changes (e.g. a widget
 * tap while the tab is on screen) update the UI in place.
 */
@Composable
fun TasbihScreen(
    themeName: ThemeName,
    hapticsEnabled: Boolean,
    tasbihStore: TasbihStore,
    modifier: Modifier = Modifier,
) {
    var count by remember { mutableStateOf(tasbihStore.count) }
    var goal by remember { mutableStateOf(tasbihStore.goal) }

    DisposableEffect(tasbihStore) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            count = tasbihStore.count
            goal = tasbihStore.goal
        }
        tasbihStore.registerListener(listener)
        onDispose { tasbihStore.unregisterListener(listener) }
    }

    AppBackground(colors = themeColors(themeName), modifier = modifier) {
        TasbihCounter(
            showGoalInput = true,
            hapticsEnabled = hapticsEnabled,
            modifier = Modifier.statusBarsPadding(),
            externalCount = count,
            externalGoal = goal,
            onCountChange = tasbihStore::setCount,
            onGoalChange = tasbihStore::setGoal,
        )
    }
}
