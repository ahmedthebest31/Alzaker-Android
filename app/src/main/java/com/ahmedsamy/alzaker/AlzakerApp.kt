package com.ahmedsamy.alzaker

import android.app.Application
import com.ahmedsamy.alzaker.data.di.AppContainer

/**
 * Application entry point. Holds the single [AppContainer] instance shared by
 * the UI, broadcast receivers and the background audio service.
 */
class AlzakerApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
