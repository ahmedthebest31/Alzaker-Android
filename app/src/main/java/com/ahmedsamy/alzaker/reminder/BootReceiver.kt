package com.ahmedsamy.alzaker.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ahmedsamy.alzaker.AlzakerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-registers reminder alarms after a device reboot or an app update
 * (self-rescheduling chains are otherwise lost because alarms do not survive
 * reboot). Reads the persisted settings and re-schedules every enabled chain.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = (context.applicationContext as AlzakerApp).container
                val settings = container.settingsRepository.settings.first()
                val scheduler = AlarmSchedulerImpl(context)
                if (settings.isTadhkirEnabled) {
                    scheduler.scheduleNext(ReminderType.TADHKIR, settings.tadhkirIntervalMinutes)
                }
                if (settings.isHikmahEnabled) {
                    scheduler.scheduleNext(ReminderType.HIKMAH, settings.hikmahIntervalMinutes)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
