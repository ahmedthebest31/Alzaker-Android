package com.ahmedsamy.alzaker.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.alzaker.ui.AppViewModel
import com.ahmedsamy.alzaker.ui.components.ActionButton
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.components.AppToast
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.LegacyColors
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors
import com.ahmedsamy.alzaker.util.Accessibility
import com.ahmedsamy.alzaker.util.Clipboard
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics
import com.ahmedsamy.alzaker.util.HomeRotation
import com.ahmedsamy.alzaker.util.Share
import kotlinx.coroutines.delay

/**
 * Home tab mirroring the legacy app/(tabs)/index.tsx: the Quran quote, a
 * rotating random dhikr (interval = clamp(100ms * text length, 5s, 7s) via
 * HomeRotation), and copy / favorite / share action buttons with the exact
 * legacy toasts and screen-reader announcements.
 */
@Composable
fun HomeScreen(
    themeName: ThemeName,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
) {
    val theme = themeColors(themeName)
    val fontSizeMultiplier = LocalFontSizeMultiplier.current
    val context = LocalContext.current
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val favoriteIds by appViewModel.favoriteIds.collectAsStateWithLifecycle()

    var currentDhikr by remember { mutableStateOf(appViewModel.dhikrRepository.getRandomDhikr()) }
    var rotationTick by remember { mutableStateOf(0) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(rotationTick) {
        val dhikr = currentDhikr
        if (dhikr != null) {
            delay(HomeRotation.intervalFor(dhikr.dhikr))
            currentDhikr = appViewModel.dhikrRepository.getRandomDhikr()
            rotationTick += 1
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            toastMessage = null
        }
    }

    val isCurrentDhikrFavorite = currentDhikr?.let { it.id in favoriteIds } ?: false

    val showToast: (String) -> Unit = { toastMessage = it }

    val copyToClipboard: () -> Unit = copyToClipboard@{
        val dhikr = currentDhikr
        if (dhikr == null || dhikr.dhikr == NO_DHIKR_AVAILABLE) {
            showToast("لا يوجد ذكر للنسخ.")
            return@copyToClipboard
        }
        val copied = runCatching { Clipboard.copyText(context, "dhikr", dhikr.dhikr) }.isSuccess
        if (copied) {
            showToast("تم نسخ الذكر إلى الحافظة.")
            Accessibility.announce(context, "تم نسخ الذكر إلى الحافظة.")
        } else {
            showToast("حدث خطأ أثناء النسخ.")
            Accessibility.announce(context, "حدث خطأ أثناء النسخ.")
        }
    }

    val shareDhikr: () -> Unit = shareDhikr@{
        val dhikr = currentDhikr
        if (dhikr == null || dhikr.dhikr == NO_DHIKR_AVAILABLE) {
            showToast("لا يوجد ذكر للمشاركة.")
            return@shareDhikr
        }
        val opened = Share.shareText(context, "✨ ${dhikr.dhikr} ✨\n\nتطبيق الذاكر")
        if (opened) {
            Accessibility.announce(context, "تم فتح قائمة المشاركة.")
        } else {
            showToast("حدث خطأ أثناء المشاركة.")
            Accessibility.announce(context, "حدث خطأ أثناء المشاركة.")
        }
    }

    val addToFavorites: () -> Unit = addToFavorites@{
        val dhikr = currentDhikr
        if (dhikr == null) {
            showToast("لا يوجد ذكر لإضافته.")
            return@addToFavorites
        }
        Haptics.trigger(context, HapticFeedbackType.ImpactMedium, settings.hapticsEnabled)
        appViewModel.toggleFavorite(dhikr.id) { isNowFavorite ->
            if (isNowFavorite) {
                showToast("تمت إضافة الذكر إلى المفضلة بنجاح!")
                Accessibility.announce(context, "تمت إضافة الذكر إلى المفضلة.")
            } else {
                showToast("تمت إزالة الذكر من المفضلة.")
                Accessibility.announce(context, "تمت إزالة الذكر من المفضلة.")
            }
        }
    }

    AppBackground(colors = theme, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "فَاذْكُرُونِي أَذْكُرْكُمْ",
                    fontSize = (32 * fontSizeMultiplier).sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = AmiriFontFamily,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                Text(
                    text = "(سورة البقرة، آية 152)",
                    fontSize = (16 * fontSizeMultiplier).sp,
                    fontFamily = AmiriFontFamily,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Crossfade(
                    targetState = currentDhikr,
                    animationSpec = tween(durationMillis = 300),
                    label = "homeDhikrFade",
                ) { dhikr ->
                    Text(
                        text = "\" ${dhikr?.dhikr ?: ""} \"",
                        fontSize = (30 * fontSizeMultiplier).sp,
                        fontFamily = AmiriFontFamily,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics {
                            contentDescription = dhikr?.dhikr ?: ""
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 15.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionButton(
                        icon = Icons.Filled.ContentCopy,
                        contentDescription = "نسخ الذكر إلى الحافظة",
                        onClick = copyToClipboard,
                        hapticsEnabled = settings.hapticsEnabled,
                    )
                    ActionButton(
                        icon = if (isCurrentDhikrFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isCurrentDhikrFavorite) "إزالة من المفضلة" else "أضف للمفضلة",
                        onClick = addToFavorites,
                        iconColor = if (isCurrentDhikrFavorite) LegacyColors.Red else Color.White,
                        hapticsEnabled = settings.hapticsEnabled,
                    )
                    ActionButton(
                        icon = Icons.Filled.Share,
                        contentDescription = "مشاركة الذكر",
                        onClick = shareDhikr,
                        hapticsEnabled = settings.hapticsEnabled,
                    )
                }
            }
        }

        AppToast(message = toastMessage.orEmpty(), visible = toastMessage != null)
    }
}

/** Sentinel the legacy app uses for an empty dhikr list. */
private const val NO_DHIKR_AVAILABLE = "لا يوجد أذكار متاحة."
