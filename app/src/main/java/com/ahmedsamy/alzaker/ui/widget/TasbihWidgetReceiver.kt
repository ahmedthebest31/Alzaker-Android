package com.ahmedsamy.alzaker.ui.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ahmedsamy.alzaker.AlzakerApp
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Handles a tap on the tasbih widget: increments the shared tasbih store,
 * plays the same haptics as the in-app counter (ImpactMedium per tap,
 * NotificationSuccess exactly when the count reaches the goal, and the count
 * keeps going past the goal like the app), then re-renders every widget
 * instance. Explicitly targeted by the widget's PendingIntent, so it is
 * non-exported. Runs in the app process, where Vibrator, SharedPreferences and
 * the haptics setting are all available.
 */
class TasbihWidgetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TasbihWidgetProvider.ACTION_TAP) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext as AlzakerApp
                val container = appContext.container
                val store = container.tasbihStore
                val newCount = store.count + 1
                store.setCount(newCount)

                val hapticsEnabled = container.settingsRepository.settings.first().hapticsEnabled
                Haptics.trigger(context, HapticFeedbackType.ImpactMedium, hapticsEnabled)
                if (store.goal > 0 && newCount == store.goal) {
                    Haptics.trigger(context, HapticFeedbackType.NotificationSuccess, hapticsEnabled)
                }

                TasbihWidgetRenderer.renderAll(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
