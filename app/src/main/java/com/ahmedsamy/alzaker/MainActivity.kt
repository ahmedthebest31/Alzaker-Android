package com.ahmedsamy.alzaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmedsamy.alzaker.ui.AppViewModel
import com.ahmedsamy.alzaker.ui.AudioPlayerViewModel
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.components.AudioOverlay
import com.ahmedsamy.alzaker.ui.navigation.AppNavController
import com.ahmedsamy.alzaker.ui.navigation.Route
import com.ahmedsamy.alzaker.ui.screens.OnboardingScreen
import com.ahmedsamy.alzaker.ui.screens.TabsScreen
import com.ahmedsamy.alzaker.ui.theme.AlzakerTheme
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
            val audioViewModel: AudioPlayerViewModel = viewModel(factory = AudioPlayerViewModel.Factory)
            val settings by appViewModel.settings.collectAsStateWithLifecycle()
            val navController = remember { AppNavController() }
            val themeName = ThemeName.fromKey(settings.themeName)

            LaunchedEffect(settings.hasLaunched) {
                navController.setInitialRoute(!settings.hasLaunched)
            }

            AlzakerTheme(
                themeName = themeName,
                fontSizeMultiplier = settings.fontSizeMultiplier,
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val route = navController.route) {
                            Route.Onboarding -> OnboardingScreen(
                                themeName = themeName,
                                onCompleteOnboarding = {
                                    appViewModel.markOnboardingComplete()
                                    navController.completeOnboarding()
                                },
                            )
                            Route.Tabs -> TabsScreen(
                                selectedTab = navController.selectedTab,
                                onTabSelected = navController::selectTab,
                                appViewModel = appViewModel,
                                themeName = themeName,
                            )
                            is Route.DhikrDetails -> RoutePlaceholder(
                                title = "تفاصيل الذكر",
                                themeName = themeName,
                            )
                        }
                        AudioOverlay(
                            currentlyPlayingText = audioViewModel.currentlyPlayingText,
                            isPlaying = audioViewModel.isPlaying,
                            themeName = themeName,
                            onStop = audioViewModel::stop,
                            onPauseResume = {
                                if (audioViewModel.isPlaying) audioViewModel.pause()
                                else audioViewModel.resume()
                            },
                        )
                    }
                }
            }

            BackHandler {
                if (!navController.goBack()) finish()
            }
        }
    }
}

/**
 * Temporary placeholder for routes whose real screens land in later steps
 * (tabs in 8e-8i, dhikr details in 8g).
 */
@Composable
private fun RoutePlaceholder(title: String, themeName: ThemeName) {
    AppBackground(colors = themeColors(themeName)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = AmiriFontFamily,
            )
        }
    }
}
