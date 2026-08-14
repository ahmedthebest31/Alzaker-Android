package com.ahmedsamy.alzaker.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavControllerTest {

    @Test
    fun initialRouteIsOnboarding() {
        assertEquals(Route.Onboarding, AppNavController().route)
    }

    @Test
    fun setInitialRouteFirstLaunchKeepsOnboarding() {
        val nav = AppNavController()
        nav.setInitialRoute(true)
        assertEquals(Route.Onboarding, nav.route)
    }

    @Test
    fun setInitialRouteReturningUserGoesToTabs() {
        val nav = AppNavController()
        nav.setInitialRoute(false)
        assertEquals(Route.Tabs, nav.route)
    }

    @Test
    fun completeOnboardingGoesToTabs() {
        val nav = AppNavController()
        nav.completeOnboarding()
        assertEquals(Route.Tabs, nav.route)
    }

    @Test
    fun dhikrDetailsPushAndBackPops() {
        val nav = AppNavController()
        nav.setInitialRoute(false)
        nav.navigateToDhikrDetails("أذكار الصباح", 3)
        assertEquals(Route.DhikrDetails("أذكار الصباح", 3), nav.route)

        assertTrue(nav.goBack())
        assertEquals(Route.Tabs, nav.route)
    }

    @Test
    fun goBackOnTabsIsNotConsumed() {
        val nav = AppNavController()
        nav.setInitialRoute(false)
        assertFalse(nav.goBack())
        assertEquals(Route.Tabs, nav.route)
    }

    @Test
    fun goBackOnOnboardingIsNotConsumed() {
        val nav = AppNavController()
        nav.setInitialRoute(true)
        assertFalse(nav.goBack())
        assertEquals(Route.Onboarding, nav.route)
    }

    @Test
    fun selectTabUpdatesSelection() {
        val nav = AppNavController()
        nav.selectTab(AppTab.SETTINGS)
        assertEquals(AppTab.SETTINGS, nav.selectedTab)
    }

    @Test
    fun nestedDetailsPopsAllTheWayBackToTabs() {
        val nav = AppNavController()
        nav.setInitialRoute(false)
        nav.navigateToDhikrDetails("أولا", 1)
        nav.navigateToDhikrDetails("ثانيا", 2)
        assertTrue(nav.goBack())
        assertEquals(Route.DhikrDetails("أولا", 1), nav.route)
        assertTrue(nav.goBack())
        assertEquals(Route.Tabs, nav.route)
        assertFalse(nav.goBack())
    }
}
