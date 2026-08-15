package com.ahmedsamy.alzaker.ui.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.ahmedsamy.alzaker.AlzakerApp
import com.ahmedsamy.alzaker.MainActivity
import com.ahmedsamy.alzaker.R
import com.ahmedsamy.alzaker.data.model.DhikrItem
import com.ahmedsamy.alzaker.util.Clipboard
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics
import com.ahmedsamy.alzaker.util.HomeRotation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Home-screen widget mirroring the Home tab: a uniform random dhikr
 * (DhikrRepository.getRandomDhikr) that rotates with the same cadence
 * (HomeRotation.intervalFor). The layout uses only stock views because OEM
 * launchers refuse custom RemoteViews classes/fonts; the fade is approximated
 * by applying the text twice (alpha 0, then alpha 1 after [FADE_IN_DELAY_MS]).
 * Rotation is driven by self-scheduling setAndAllowWhileIdle alarms (no exact
 * alarm needed; Doze throttles it while the screen is off). Tapping the widget
 * opens MainActivity; a small copy button copies the currently displayed dhikr
 * to the clipboard (same behavior as the in-app copy: haptic + toast). Rotation
 * resumes on APPWIDGET_UPDATE, BOOT_COMPLETED and MY_PACKAGE_REPLACED.
 */
class DhikrWidgetProvider : AppWidgetProvider() {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ROTATE,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        refresh(context.applicationContext)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_COPY -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        handleCopy(context.applicationContext)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                refresh(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDisabled(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildRotatePendingIntent(context))
    }

    private fun refresh(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DhikrWidgetProvider::class.java),
            )
            if (widgetIds.isEmpty()) return

            val dhikr = (context as AlzakerApp).container.dhikrRepository.getRandomDhikr()
            val text = dhikr?.dhikr ?: NO_DHIKR_AVAILABLE

            rememberCurrentDhikr(context, text)

            applyViews(context, appWidgetManager, widgetIds, text, alpha = 0f)
            mainHandler.postDelayed(
                { applyViews(context, appWidgetManager, widgetIds, text, alpha = 1f) },
                FADE_IN_DELAY_MS,
            )

            scheduleNextRotation(context, dhikr)
        } catch (error: Exception) {
            Log.w(TAG, "Widget refresh failed", error)
        }
    }

    private fun applyViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetIds: IntArray,
        text: String,
        alpha: Float,
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_dzikr).apply {
            setTextViewText(R.id.widget_dhikr_text, text)
            setFloat(R.id.widget_dhikr_text, "setAlpha", alpha)
            setOnClickPendingIntent(R.id.widget_root, buildOpenAppPendingIntent(context))
            setOnClickPendingIntent(R.id.widget_dhikr_copy, buildCopyPendingIntent(context))
        }
        appWidgetManager.updateAppWidget(widgetIds, views)
    }

    private fun scheduleNextRotation(context: Context, dhikr: DhikrItem?) {
        val interval = if (dhikr != null) {
            HomeRotation.intervalFor(dhikr.dhikr)
        } else {
            HomeRotation.MIN_INTERVAL_MS
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + interval,
            buildRotatePendingIntent(context),
        )
    }

    private fun buildRotatePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DhikrWidgetProvider::class.java).setAction(ACTION_ROTATE)
        return PendingIntent.getBroadcast(
            context,
            ROTATE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildOpenAppPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun buildCopyPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DhikrWidgetProvider::class.java).setAction(ACTION_COPY)
        return PendingIntent.getBroadcast(
            context,
            COPY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Copies the currently displayed dhikr to the clipboard with the in-app
     *  haptic and toast. The receiver runs in the app process, so Vibrator and
     *  the haptics setting are available; on Android 12+ a background Toast may
     *  be suppressed, in which case the haptic still confirms the copy. */
    private suspend fun handleCopy(context: Context) {
        val text = currentDhikr(context) ?: return
        Clipboard.copyText(context, "dhikr", text)
        val hapticsEnabled = (context as AlzakerApp).container.settingsRepository.settings.first().hapticsEnabled
        Haptics.trigger(context, HapticFeedbackType.NotificationSuccess, hapticsEnabled)
        Toast.makeText(context, "تم نسخ الذكر إلى الحافظة.", Toast.LENGTH_SHORT).show()
    }

    private fun rememberCurrentDhikr(context: Context, text: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_CURRENT_DHIKR, text).apply()
    }

    private fun currentDhikr(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CURRENT_DHIKR, null)

    companion object {
        /** Broadcast action that triggers the next widget rotation. */
        const val ACTION_ROTATE = "com.ahmedsamy.alzaker.action.ROTATE_DHIKR_WIDGET"

        /** Broadcast action that copies the currently displayed dhikr. */
        const val ACTION_COPY = "com.ahmedsamy.alzaker.action.COPY_DHIKR_WIDGET"

        private const val TAG = "DhikrWidget"
        private const val ROTATE_REQUEST_CODE = 101
        private const val OPEN_APP_REQUEST_CODE = 102
        private const val COPY_REQUEST_CODE = 103
        private const val NO_DHIKR_AVAILABLE = "لا يوجد أذكار متاحة."
        private const val FADE_IN_DELAY_MS = 350L
        private const val PREFS_NAME = "dhikr_widget_store"
        private const val KEY_CURRENT_DHIKR = "current_dhikr"
    }
}
