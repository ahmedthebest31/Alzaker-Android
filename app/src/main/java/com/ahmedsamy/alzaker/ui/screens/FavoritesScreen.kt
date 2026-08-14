package com.ahmedsamy.alzaker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.alzaker.data.model.DhikrItem
import com.ahmedsamy.alzaker.ui.AppViewModel
import com.ahmedsamy.alzaker.ui.AudioPlayerViewModel
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.components.DhikrCard
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors

/**
 * Favorites tab mirroring the legacy app/(tabs)/favorites.tsx: a title
 * header, then the list of favorited dhikr cards (DhikrCard), a loading
 * indicator while the favorites set is still hydrating, and the exact empty
 * message when there are none. The list is derived reactively from the
 * favoriteIds StateFlow, so unfavoriting a card removes it instantly (the
 * legacy reloaded on every focus via useFocusEffect).
 */
@Composable
fun FavoritesScreen(
    themeName: ThemeName,
    appViewModel: AppViewModel,
    audioViewModel: AudioPlayerViewModel,
    onOpenCounter: (DhikrItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val favoriteIds by appViewModel.favoriteIds.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        isLoading = false
    }

    val favorites = remember(favoriteIds) {
        appViewModel.dhikrRepository.allDhikr.filter { it.id in favoriteIds }
    }

    AppBackground(colors = themeColors(themeName), modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "أذكاري المفضلة",
                    color = Color.White,
                    fontSize = (32 * LocalFontSizeMultiplier.current).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFontFamily,
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Text(
                                text = "جاري تحميل الأذكار...",
                                color = Color.White,
                                fontSize = (18 * LocalFontSizeMultiplier.current).sp,
                                fontFamily = AmiriFontFamily,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                        }
                    }
                }

                favorites.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "لم تقم بإضافة أي أذكار للمفضلة بعد.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = (18 * LocalFontSizeMultiplier.current).sp,
                            fontFamily = AmiriFontFamily,
                            textAlign = TextAlign.Center,
                            lineHeight = 25.sp,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(favorites, key = { it.id }) { item ->
                            DhikrCard(
                                item = item,
                                isFavorite = true,
                                isPlaying = audioViewModel.currentlyPlayingId == item.id,
                                onFavoriteToggle = {
                                    appViewModel.toggleFavorite(item.id) { }
                                },
                                onAudioToggle = {
                                    val url = item.audioUrl
                                    if (url != null) {
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
    }
}
