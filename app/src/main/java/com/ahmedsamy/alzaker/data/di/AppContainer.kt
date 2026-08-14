package com.ahmedsamy.alzaker.data.di

import android.content.Context
import com.ahmedsamy.alzaker.data.local.LocalDhikrDataSource
import com.ahmedsamy.alzaker.data.local.alzakerDataStore
import com.ahmedsamy.alzaker.data.repository.DhikrRepository
import com.ahmedsamy.alzaker.data.repository.DhikrRepositoryImpl
import com.ahmedsamy.alzaker.data.repository.FavoritesRepository
import com.ahmedsamy.alzaker.data.repository.FavoritesRepositoryImpl
import com.ahmedsamy.alzaker.data.repository.SettingsRepository
import com.ahmedsamy.alzaker.data.repository.SettingsRepositoryImpl

/**
 * Manual dependency container (no DI framework, keeping the build offline-safe).
 * A single instance is held by the Application class.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val dataSource = LocalDhikrDataSource(appContext)

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(appContext.alzakerDataStore)
    }

    val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepositoryImpl(appContext.alzakerDataStore)
    }

    val dhikrRepository: DhikrRepository by lazy {
        DhikrRepositoryImpl(
            dhikrList = dataSource.loadDhikrList(),
            audioDropTexts = dataSource.loadAudioDropTexts(),
        )
    }
}
