package com.ahmedsamy.alzaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedsamy.alzaker.data.model.DhikrItem
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.Gold
import com.ahmedsamy.alzaker.ui.theme.LegacyColors
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics

/**
 * A single dhikr entry card, mirroring the legacy DhikrCard: favorite heart,
 * repeat-count circle and an audio play/stop toggle. Tapping the card body
 * opens the dhikr counter.
 *
 * Screen-reader structure (mergeDescendants = false on the card): the card
 * itself announces the dhikr text and opens the counter, the repeat count is a
 * separate focusable node ("عدد مرات التكرار: N"), and the favorite heart and
 * audio button are independent buttons. The audio button is enlarged (56dp)
 * for elderly users and its label changes to "إيقاف الذكر الحالي" while
 * playing. All state (favorite/playing) is owned by the caller.
 */
@Composable
fun DhikrCard(
    item: DhikrItem,
    isFavorite: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean = false,
    onFavoriteToggle: () -> Unit,
    onAudioToggle: () -> Unit,
    onOpenCounter: () -> Unit,
    modifier: Modifier = Modifier,
    hapticsEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val fontSizeMultiplier = LocalFontSizeMultiplier.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(15.dp))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(15.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    Haptics.trigger(context, HapticFeedbackType.ImpactLight, hapticsEnabled)
                    onOpenCounter()
                },
            )
            .semantics(mergeDescendants = false) {
                contentDescription = "${item.dhikr}. اضغط لفتح صفحة الذكر"
            }
            .padding(16.dp),
    ) {
        Text(
            text = item.dhikr,
            color = Color.White,
            fontSize = (20 * fontSizeMultiplier).sp,
            fontFamily = AmiriFontFamily,
            lineHeight = 32.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clearAndSetSemantics {},
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            Haptics.trigger(context, HapticFeedbackType.ImpactMedium, hapticsEnabled)
                            onFavoriteToggle()
                        },
                    )
                    .semantics {
                        contentDescription = if (isFavorite) "إزالة من المفضلة" else "إضافة إلى المفضلة"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) LegacyColors.Red else Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(min = 52.dp)
                    .height(52.dp)
                    .background(Gold.copy(alpha = 0.1f), RoundedCornerShape(26.dp))
                    .border(width = 2.dp, color = Gold, shape = RoundedCornerShape(26.dp))
                    .padding(horizontal = 12.dp)
                    .focusable()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "عدد مرات التكرار: ${item.repeat}"
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "×${item.repeat}",
                    color = Gold,
                    fontSize = (20 * fontSizeMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFontFamily,
                )
            }

            if (item.audioUrl != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPlaying || isBuffering -> Gold
                                else -> Color.White.copy(alpha = 0.15f)
                            },
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                Haptics.trigger(context, HapticFeedbackType.ImpactLight, hapticsEnabled)
                                onAudioToggle()
                            },
                        )
                        .semantics {
                            contentDescription = if (isPlaying) {
                                "إيقاف الذكر الحالي صوتياً"
                            } else {
                                "تشغيل الذكر صوتياً"
                            }
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = if (isPlaying) LegacyColors.Black else Gold,
                            strokeWidth = 3.dp,
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Outlined.StopCircle else Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            tint = if (isPlaying || isBuffering) LegacyColors.Black else primaryColor,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }
        }
    }
}
