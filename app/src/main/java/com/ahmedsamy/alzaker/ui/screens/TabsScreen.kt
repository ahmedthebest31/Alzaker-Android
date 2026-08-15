package com.ahmedsamy.alzaker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.alzaker.ui.AppViewModel
import com.ahmedsamy.alzaker.ui.AudioPlayerViewModel
import com.ahmedsamy.alzaker.ui.navigation.AppTab
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics

/**
 * The five-tab container mirroring the legacy app/(tabs)/_layout.tsx: a
 * translucent dark bottom bar (rgba(0,0,0,0.4)) with white labels and
 * filled/outlined icons, an ImpactLight haptic on every tab press, and the
 * active tab's screen above it. Screens arrive step by step; the tabs that
 * have not landed yet show a centered placeholder.
 */
@Composable
fun TabsScreen(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    appViewModel: AppViewModel,
    audioViewModel: AudioPlayerViewModel,
    themeName: ThemeName,
    onOpenDhikrDetails: (dhikr: String, repeat: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(tween(durationMillis = 250)) togetherWith fadeOut(tween(durationMillis = 150))
            },
            label = "tabContent",
        ) { tab ->
            when (tab) {
                AppTab.HOME -> HomeScreen(
                    themeName = themeName,
                    appViewModel = appViewModel,
                    audioViewModel = audioViewModel,
                )
                AppTab.TASBIH -> TasbihScreen(
                    themeName = themeName,
                    hapticsEnabled = settings.hapticsEnabled,
                )
                AppTab.ADHKAR -> AdhkarScreen(
                    themeName = themeName,
                    appViewModel = appViewModel,
                    audioViewModel = audioViewModel,
                    onOpenCounter = { item -> onOpenDhikrDetails(item.dhikr, item.repeat) },
                )
                AppTab.FAVORITES -> FavoritesScreen(
                    themeName = themeName,
                    appViewModel = appViewModel,
                    audioViewModel = audioViewModel,
                    onOpenCounter = { item -> onOpenDhikrDetails(item.dhikr, item.repeat) },
                )
                AppTab.SETTINGS -> SettingsScreen(
                    themeName = themeName,
                    appViewModel = appViewModel,
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF000000).copy(alpha = 0.4f))
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTab.entries.reversed().forEach { tab ->
                TabItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = {
                        Haptics.trigger(context, HapticFeedbackType.ImpactLight, settings.hapticsEnabled)
                        onTabSelected(tab)
                    },
                )
            }
        }
    }
}

/** The legacy tab bar labels. */
private val AppTab.title: String
    get() = when (this) {
        AppTab.HOME -> "الرئيسية"
        AppTab.TASBIH -> "المسبحة"
        AppTab.ADHKAR -> "الأذكار"
        AppTab.FAVORITES -> "المفضلة"
        AppTab.SETTINGS -> "إعدادات"
    }

/** Filled icon for the focused tab, mirroring the legacy Ionicons variants. */
private val AppTab.filledIcon: ImageVector
    get() = when (this) {
        AppTab.HOME -> Icons.Filled.Home
        AppTab.TASBIH -> Icons.Filled.FrontHand
        AppTab.ADHKAR -> Icons.AutoMirrored.Filled.MenuBook
        AppTab.FAVORITES -> Icons.Filled.Favorite
        AppTab.SETTINGS -> Icons.Filled.Settings
    }

/** Outlined icon for the unfocused tab. */
private val AppTab.outlinedIcon: ImageVector
    get() = when (this) {
        AppTab.HOME -> Icons.Outlined.Home
        AppTab.TASBIH -> Icons.Outlined.FrontHand
        AppTab.ADHKAR -> Icons.AutoMirrored.Outlined.MenuBook
        AppTab.FAVORITES -> Icons.Outlined.Favorite
        AppTab.SETTINGS -> Icons.Outlined.Settings
    }

@Composable
private fun TabItem(
    tab: AppTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .semantics {
                contentDescription = tab.title
                role = Role.Tab
                this.selected = selected
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = tab.title,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = AmiriFontFamily,
            textAlign = TextAlign.Center,
        )
    }
}
