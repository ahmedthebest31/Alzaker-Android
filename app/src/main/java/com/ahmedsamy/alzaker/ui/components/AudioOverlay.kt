package com.ahmedsamy.alzaker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.LegacyColors
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors

/**
 * Bottom audio bar mirroring the legacy AudioOverlay: slides up above the tab
 * bar while a dhikr is playing and shows the current dhikr text plus a close
 * (X) button that stops playback and dismisses the bar, and a play/pause
 * control. The dark translucent background approximates the legacy expo-blur
 * dark tint. [themeName] drives the highContrast button colors.
 */
@Composable
fun AudioOverlay(
    currentlyPlayingText: String?,
    isPlaying: Boolean,
    themeName: ThemeName,
    onStop: () -> Unit,
    onPauseResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontSizeMultiplier = LocalFontSizeMultiplier.current
    val theme = themeColors(themeName)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = currentlyPlayingText != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 10.dp, end = 10.dp, bottom = 90.dp),
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ) { it },
            exit = slideOutVertically(animationSpec = tween(durationMillis = 300)) { it },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A1A1A).copy(alpha = 0.92f))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "الذكر الجاري تشغيله: ${currentlyPlayingText.orEmpty()}"
                        },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = currentlyPlayingText.orEmpty(),
                        color = Color.White,
                        fontSize = (16 * fontSizeMultiplier).sp,
                        fontFamily = AmiriFontFamily,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onStop)
                        .semantics {
                            contentDescription = "إغلاق المشغل وإيقاف الذكر الصوتي"
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (themeName == ThemeName.HIGH_CONTRAST) theme.accent else Color.White)
                        .clickable(onClick = onPauseResume)
                        .semantics {
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "استئناف التشغيل"
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = if (themeName == ThemeName.HIGH_CONTRAST) LegacyColors.Black else theme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}
