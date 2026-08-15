package com.ahmedsamy.alzaker.ui.widget

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import com.ahmedsamy.alzaker.AlzakerApp

/**
 * Small dialog activity opened by the tasbih widget's reset button. RemoteViews
 * cannot capture a long-press (the launcher owns it), so reset goes through
 * this confirmation dialog: 'تصفير العداد؟' with إلغاء / تصفير. Confirming
 * zeroes the shared store and re-renders every widget instance. Non-exported
 * (only the app's own PendingIntent reaches it) and excluded from recents.
 */
class TasbihResetActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContext = applicationContext as AlzakerApp
        val store = appContext.container.tasbihStore

        AlertDialog.Builder(this)
            .setTitle("تصفير العداد؟")
            .setMessage("سيتم إعادة ضبط عدد التسبيحات إلى 0.")
            .setNegativeButton("إلغاء") { _, _ -> finish() }
            .setPositiveButton("تصفير") { _, _ ->
                store.setCount(0)
                TasbihWidgetRenderer.renderAll(applicationContext)
                finish()
            }
            .setOnCancelListener { finish() }
            .show()
    }
}
