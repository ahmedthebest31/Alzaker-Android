package com.ahmedsamy.alzaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmedsamy.alzaker.ui.AppViewModel
import com.ahmedsamy.alzaker.ui.AudioPlayerViewModel
import com.ahmedsamy.alzaker.ui.components.AudioOverlay
import com.ahmedsamy.alzaker.ui.navigation.AppNavController
import com.ahmedsamy.alzaker.ui.navigation.Route
import com.ahmedsamy.alzaker.ui.screens.DhikrDetailsScreen
import com.ahmedsamy.alzaker.ui.screens.OnboardingScreen
import com.ahmedsamy.alzaker.ui.screens.TabsScreen
import com.ahmedsamy.alzaker.ui.theme.AlzakerTheme
import com.ahmedsamy.alzaker.ui.theme.ThemeName

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
                        AnimatedContent(
                            targetState = navController.route,
                            transitionSpec = {
                                fadeIn(tween(durationMillis = 300)) togetherWith fadeOut(tween(durationMillis = 200))
                            },
                            label = "routeContent",
                        ) { route ->
                            when (route) {
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
                                    audioViewModel = audioViewModel,
                                    themeName = themeName,
                                    onOpenDhikrDetails = navController::navigateToDhikrDetails,
                                )
                                is Route.DhikrDetails -> DhikrDetailsScreen(
                                    dhikr = route.dhikr,
                                    repeat = route.repeat,
                                    themeName = themeName,
                                    hapticsEnabled = settings.hapticsEnabled,
                                )
                            }
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
