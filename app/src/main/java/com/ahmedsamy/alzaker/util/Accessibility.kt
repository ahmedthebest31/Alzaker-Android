package com.ahmedsamy.alzaker.util

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

/**
 * Screen-reader announcement helper mirroring the legacy
 * AccessibilityInfo.announceForAccessibility calls on the home screen.
 */
object Accessibility {

    fun announce(context: Context, message: String) {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return
        if (!manager.isEnabled) return
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT).apply {
            text.add(message)
            packageName = context.packageName
            className = context.javaClass.name
        }
        manager.sendAccessibilityEvent(event)
    }
}
