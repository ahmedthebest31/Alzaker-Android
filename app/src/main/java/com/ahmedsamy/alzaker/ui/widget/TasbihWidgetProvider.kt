package com.ahmedsamy.alzaker.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.ahmedsamy.alzaker.AlzakerApp
import com.ahmedsamy.alzaker.R
import com.ahmedsamy.alzaker.util.TasbihProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "TasbihWidget"

/**
 * Home-screen tasbih widget mirroring the tasbih tab: the shared count/goal
 * from [com.ahmedsamy.alzaker.data.local.TasbihStore], a thin horizontal
 * progress bar, a decorative "N/M" label and a small reset button. Tapping the
 * card is a broadcast handled by [TasbihWidgetReceiver]; the reset button opens
 * [TasbihResetActivity]. Rendering uses stock views + the default font only
 * (OEM-safe, per the dhikr-widget V2 lesson) and follows system colors on
 * API 31+ via version-qualified @color resources.
 *
 * The widget stays in sync with the tasbih tab through a single in-process
 * SharedPreferences listener ([TasbihWidgetRenderer.attach]); both live in the
 * app process, so a tap re-renders immediately and the tab's changes land on
 * the widget without waiting for a system update.
 */
class TasbihWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TasbihWidgetRenderer.attach(context.applicationContext)
                TasbihWidgetRenderer.renderAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        TasbihWidgetRenderer.attach(context.applicationContext)
                        TasbihWidgetRenderer.renderAll(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            else -> super.onReceive(context, intent)
        }
    }

    override fun onDisabled(context: Context) {
        TasbihWidgetRenderer.detach(context)
    }

    companion object {
        /** Broadcast action for a tap on the widget card ([TasbihWidgetReceiver]). */
        const val ACTION_TAP = "com.ahmedsamy.alzaker.action.TASBIH_WIDGET_TAP"

        const val TAP_REQUEST_CODE = 201
        const val RESET_REQUEST_CODE = 202
    }
}

/**
 * Renders every tasbih widget instance from the shared store and keeps the one
 * in-process listener that re-renders the widgets while the user changes the
 * count/goal on the tasbih tab. The provider instance is recreated for every
 * broadcast, so the listener lives here (a process-wide singleton) instead.
 */
internal object TasbihWidgetRenderer {

    private var registered = false
    private var appContext: Context? = null
    private val storeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        val context = appContext ?: return@OnSharedPreferenceChangeListener
        runCatching { renderAll(context) }
            .onFailure { Log.w(TAG, "Store change render failed", it) }
    }

    @Synchronized
    fun attach(context: Context) {
        appContext = context.applicationContext
        if (registered) return
        (appContext as AlzakerApp).container.tasbihStore.registerListener(storeListener)
        registered = true
    }

    @Synchronized
    fun detach(context: Context) {
        val ctx = appContext ?: return
        (ctx as AlzakerApp).container.tasbihStore.unregisterListener(storeListener)
        registered = false
        appContext = null
    }

    fun renderAll(context: Context) {
        try {
            val appContext = context.applicationContext as AlzakerApp
            val store = appContext.container.tasbihStore
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(
                ComponentName(context, TasbihWidgetProvider::class.java),
            )
            if (widgetIds.isEmpty()) return

            val count = store.count
            val goal = store.goal
            val reached = TasbihProgress.isGoalReached(count, goal)

            val views = RemoteViews(context.packageName, R.layout.widget_tasbih).apply {
                setTextViewText(R.id.widget_tasbih_count, count.toString())
                setTextColor(
                    R.id.widget_tasbih_count,
                    context.getColor(if (reached) R.color.widget_count_reached else R.color.widget_count),
                )
                setContentDescription(R.id.widget_tasbih_count, TasbihProgress.contentDescription(count, goal))
                setOnClickPendingIntent(R.id.widget_tasbih_root, buildTapPendingIntent(context))
                setOnClickPendingIntent(R.id.widget_tasbih_reset, buildResetPendingIntent(context))

                if (goal > 0) {
                    setInt(R.id.widget_tasbih_progress, "setVisibility", View.VISIBLE)
                    setInt(R.id.widget_tasbih_progress, "setMax", goal)
                    setInt(R.id.widget_tasbih_progress, "setProgress", count.coerceAtMost(goal))
                    setTextViewText(R.id.widget_tasbih_label, TasbihProgress.progressLabel(count, goal).orEmpty())
                    setInt(R.id.widget_tasbih_label, "setVisibility", View.VISIBLE)
                } else {
                    setInt(R.id.widget_tasbih_progress, "setVisibility", View.GONE)
                    setInt(R.id.widget_tasbih_label, "setVisibility", View.GONE)
                }
            }
            widgetManager.updateAppWidget(widgetIds, views)
        } catch (error: Exception) {
            Log.w(TAG, "Widget render failed", error)
        }
    }

    private fun buildTapPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TasbihWidgetReceiver::class.java)
            .setAction(TasbihWidgetProvider.ACTION_TAP)
        return PendingIntent.getBroadcast(
            context,
            TasbihWidgetProvider.TAP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildResetPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TasbihResetActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            TasbihWidgetProvider.RESET_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
