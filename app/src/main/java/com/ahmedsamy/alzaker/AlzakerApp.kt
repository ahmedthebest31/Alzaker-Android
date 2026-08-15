package com.ahmedsamy.alzaker

import android.app.Application
import com.ahmedsamy.alzaker.data.di.AppContainer
import com.ahmedsamy.alzaker.reminder.NotificationChannels

/**
 * Application entry point. Holds the single [AppContainer] instance shared by
 * the UI, broadcast receivers and the background audio service, and creates
 * the notification channels once at startup.
 */
class AlzakerApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
    }
}
