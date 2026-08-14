package com.ahmedsamy.alzaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.ahmedsamy.alzaker.ui.theme.AlzakerThemeColors

/**
 * Full-screen vertical gradient background, mirroring the legacy
 * expo-linear-gradient wrapper (`colors=[primary, primaryLight]`, top to
 * bottom) that every legacy screen paints underneath its content.
 */
@Composable
fun AppBackground(
    colors: AlzakerThemeColors,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(colors.primary, colors.primaryLight)),
            ),
        content = content,
    )
}
