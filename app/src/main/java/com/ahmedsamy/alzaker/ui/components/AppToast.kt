package com.ahmedsamy.alzaker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier

/**
 * Transient toast mirroring the legacy Toast component: a dark rounded pill
 * that fades in over 400 ms near the bottom of the screen and is announced to
 * screen readers via a polite live region. Call it inside a [Box] (e.g. an
 * [AppBackground]) so its bottom alignment resolves.
 */
@Composable
fun BoxScope.AppToast(
    message: String,
    visible: Boolean,
) {
    val fontSizeMultiplier = LocalFontSizeMultiplier.current
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(start = 20.dp, end = 20.dp, bottom = 100.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        enter = fadeIn(tween(durationMillis = 400)),
        exit = fadeOut(tween(durationMillis = 400)),
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(25.dp), clip = false)
                .background(Color(0xFF1E1E1E).copy(alpha = 0.95f), RoundedCornerShape(25.dp))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(25.dp))
                .padding(vertical = 12.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = (16 * fontSizeMultiplier).sp,
                fontFamily = AmiriFontFamily,
                textAlign = TextAlign.Center,
            )
        }
    }
}
