package com.ahmedsamy.alzaker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.alzaker.data.model.DhikrItem
import com.ahmedsamy.alzaker.data.repository.DhikrRepository
import com.ahmedsamy.alzaker.ui.AppViewModel
import com.ahmedsamy.alzaker.ui.AudioPlayerViewModel
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.components.DhikrCard
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics

/**
 * Adhkar tab mirroring the legacy app/(tabs)/adhkar.tsx: a horizontal
 * category radio filter ('الكل' + distinct categories) above a list of
 * dhikr cards. Each card opens the dedicated dhikr page on tap (like the
 * favorites screen), the repeat count is a separate screen-reader element,
 * and the enlarged audio button toggles playback through
 * [AudioPlayerViewModel]. The card is not a checkbox: its semantics are the
 * card action (open page) plus independent favorite / count / play buttons.
 */
@Composable
fun AdhkarScreen(
    themeName: ThemeName,
    appViewModel: AppViewModel,
    audioViewModel: AudioPlayerViewModel,
    onOpenCounter: (DhikrItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val favoriteIds by appViewModel.favoriteIds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf(DhikrRepository.ALL_CATEGORIES) }

    val filtered: List<DhikrItem> = remember(selectedCategory) {
        if (selectedCategory == DhikrRepository.ALL_CATEGORIES) {
            appViewModel.dhikrRepository.allDhikr
        } else {
            appViewModel.dhikrRepository.getDhikrByCategory(selectedCategory)
        }
    }

    AppBackground(colors = themeColors(themeName), modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            CategoryFilter(
                categories = appViewModel.dhikrRepository.categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
                hapticsEnabled = settings.hapticsEnabled,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filtered, key = { it.id }) { item ->
                    DhikrCard(
                        item = item,
                        isFavorite = item.id in favoriteIds,
                        isPlaying = audioViewModel.currentlyPlayingId == item.id,
                        isBuffering = audioViewModel.isPreparing && audioViewModel.currentlyPlayingId == item.id,
                        onFavoriteToggle = { appViewModel.toggleFavorite(item.id) { } },
                        onAudioToggle = {
                            val url = item.audioUrl
                            if (url != null) {
                                Haptics.trigger(context, HapticFeedbackType.ImpactLight, settings.hapticsEnabled)
                                audioViewModel.toggle(item.id, url, item.dhikr, settings.audioVolume)
                            }
                        },
                        onOpenCounter = { onOpenCounter(item) },
                        hapticsEnabled = settings.hapticsEnabled,
                    )
                }
            }
        }
    }
}

/** Horizontal category radio group, mirroring the legacy CategoryFilter. */
@Composable
private fun CategoryFilter(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    hapticsEnabled: Boolean,
) {
    val context = LocalContext.current
    val fontSizeMultiplier = LocalFontSizeMultiplier.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 14.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "تصفية الأذكار حسب الفئة"
            },
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            categories.forEach { category ->
                val isActive = category == selected
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = if (isActive) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(24.dp),
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                Haptics.trigger(context, HapticFeedbackType.ImpactLight, hapticsEnabled)
                                onSelect(category)
                            },
                        )
                        .padding(vertical = 12.dp, horizontal = 20.dp)
                        .semantics(mergeDescendants = true) {
                            role = Role.RadioButton
                            this.selected = isActive
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = category,
                        color = Color.White,
                        fontSize = (18 * fontSizeMultiplier).sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = AmiriFontFamily,
                    )
                }
            }
        }
    }
}
