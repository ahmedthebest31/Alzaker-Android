package com.ahmedsamy.alzaker.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The five bottom tabs, in the same order as the legacy expo-router tab bar.
 */
enum class AppTab { HOME, TASBIH, ADHKAR, FAVORITES, SETTINGS }

/**
 * Top-level destinations. The tab container is a single [Tabs] route whose
 * active tab is tracked separately in [AppNavController.selectedTab].
 */
sealed interface Route {
    data object Onboarding : Route
    data object Tabs : Route
    data class DhikrDetails(val dhikr: String, val repeat: Int) : Route
}

/**
 * Minimal manual navigation controller (decision D7): one current route, a
 * small back stack only for pushed detail routes, and system-back support.
 */
class AppNavController {

    var route: Route by mutableStateOf<Route>(Route.Onboarding)
        private set

    var selectedTab: AppTab by mutableStateOf(AppTab.HOME)
        private set

    private val backStack = mutableStateListOf<Route>()

    /** Called once the first-launch flag is known. */
    fun setInitialRoute(isFirstLaunch: Boolean) {
        backStack.clear()
        route = if (isFirstLaunch) Route.Onboarding else Route.Tabs
    }

    fun completeOnboarding() {
        backStack.clear()
        route = Route.Tabs
    }

    fun navigateToDhikrDetails(dhikr: String, repeat: Int) {
        backStack.add(route)
        route = Route.DhikrDetails(dhikr, repeat)
    }

    fun selectTab(tab: AppTab) {
        selectedTab = tab
    }

    /** Returns true when the system back press was consumed by navigation. */
    fun goBack(): Boolean {
        if (route is Route.DhikrDetails && backStack.isNotEmpty()) {
            route = backStack.removeAt(backStack.size - 1)
            return true
        }
        return false
    }
}
