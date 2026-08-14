package com.ahmedsamy.alzaker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Opens the system screens used by the battery-optimization guidance, matching
 * the legacy Linking.openSettings() behaviour.
 */
object Battery {

    /** App details settings page (per-app battery / force-stop / autostart entry). */
    fun openAppDetailsSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
    }

    /** The battery-optimization allow-list screen for all apps. */
    fun openBatteryOptimizationSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
    }
}
