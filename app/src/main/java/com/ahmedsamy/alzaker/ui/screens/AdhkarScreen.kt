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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.alzaker.data.model.DhikrItem
import com.ahmedsamy.alzaker.data.repository.DhikrRepository
import com.ahmedsamy.alzaker.ui.AppViewModel
import com.ahmedsamy.alzaker.ui.AudioPlayerViewModel
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.Gold
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics

/**
 * Adhkar tab mirroring the legacy app/(tabs)/adhkar.tsx: a horizontal
 * category radio filter ('الكل' + distinct categories) above a list of
 * dhikr rows. Rows with an audio_url toggle playback through
 * [AudioPlayerViewModel] (legacy toggleDhikrSound); rows without audio are
 * disabled. Buffering is approximated with the legacy 'جارٍ التحميل' label.
 */
@Composable
fun AdhkarScreen(
    themeName: ThemeName,
    appViewModel: AppViewModel,
    audioViewModel: AudioPlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
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
        Column(modifier = Modifier.fillMaxSize()) {
            CategoryFilter(
                categories = appViewModel.dhikrRepository.categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
                hapticsEnabled = settings.hapticsEnabled,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filtered, key = { it.id }) { item ->
                    DhikrRow(
                        item = item,
                        isActive = audioViewModel.currentlyPlayingId == item.id,
                        isPlaying = audioViewModel.isPlaying,
                        isBuffering = audioViewModel.isPreparing,
                        onPress = {
                            val url = item.audioUrl ?: return@DhikrRow
                            Haptics.trigger(context, HapticFeedbackType.ImpactLight, settings.hapticsEnabled)
                            audioViewModel.toggle(item.id, url, item.dhikr, settings.audioVolume)
                        },
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
            .padding(bottom = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "تصفية الأذكار حسب الفئة"
            },
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                val isActive = category == selected
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = if (isActive) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                Haptics.trigger(context, HapticFeedbackType.ImpactLight, hapticsEnabled)
                                onSelect(category)
                            },
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .semantics(mergeDescendants = true) {
                            role = Role.RadioButton
                            this.selected = isActive
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = category,
                        color = if (isActive) Color.White else Color.White,
                        fontSize = (16 * fontSizeMultiplier).sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = AmiriFontFamily,
                    )
                }
            }
        }
    }
}

/** One dhikr list row, mirroring the legacy DhikrRow. */
@Composable
private fun DhikrRow(
    item: DhikrItem,
    isActive: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onPress: () -> Unit,
) {
    val fontSizeMultiplier = LocalFontSizeMultiplier.current
    val hasAudio = item.audioUrl != null

    val playStateLabel = when {
        isActive && isBuffering -> "جارٍ التحميل"
        isActive && isPlaying -> "قيد التشغيل. اضغط للإيقاف المؤقت"
        isActive -> "متوقف مؤقتاً. اضغط للاستئناف"
        hasAudio -> "اضغط لتشغيل الصوت"
        else -> ""
    }

    val a11yLabel = if (hasAudio) {
        "${item.dhikr}. $playStateLabel."
    } else {
        "${item.dhikr}. لا يتوفر صوت."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isActive) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
            )
            .border(
                width = 1.dp,
                color = if (isActive) Gold else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp),
            )
            .alpha(if (hasAudio) 1f else 0.55f)
            .let { base ->
                if (hasAudio) {
                    base.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onPress,
                    )
                } else {
                    base
                }
            }
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                this.selected = isActive
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasAudio) {
            Box(
                modifier = Modifier.padding(end = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isActive && isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = Gold,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = if (isActive && isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = if (isActive) Gold else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }

        Text(
            text = item.dhikr,
            color = if (isActive) Gold else Color.White,
            fontSize = (18 * fontSizeMultiplier).sp,
            fontFamily = AmiriFontFamily,
            lineHeight = 30.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )

        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "×${item.repeat}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = AmiriFontFamily,
            )
        }
    }
}
