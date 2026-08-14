package com.ahmedsamy.alzaker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Single Preferences DataStore backing all persisted app state (settings and
 * favorites). Must be a top-level property so the delegate is unique per app.
 */
val Context.alzakerDataStore: DataStore<Preferences> by preferencesDataStore(name = "alzaker")
