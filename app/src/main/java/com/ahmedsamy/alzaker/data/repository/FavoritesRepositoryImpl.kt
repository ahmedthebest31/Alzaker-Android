package com.ahmedsamy.alzaker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed favorites repository. Ids are stored as a string set so the
 * value is directly observable through [favoriteIds].
 */
class FavoritesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : FavoritesRepository {

    override val favoriteIds: Flow<Set<Int>> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs ->
            prefs[Keys.FAVORITES]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        }

    override suspend fun isFavorite(id: Int): Boolean = favoriteIds.first().contains(id)

    override suspend fun toggleFavorite(id: Int): Boolean {
        var added = false
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]
                ?.mapNotNull { it.toIntOrNull() }
                ?.toMutableSet()
                ?: mutableSetOf()
            added = if (id in current) {
                current.remove(id)
                false
            } else {
                current.add(id)
                true
            }
            prefs[Keys.FAVORITES] = current.map { it.toString() }.toSet()
        }
        return added
    }

    override suspend fun addFavorite(id: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            current.add(id.toString())
            prefs[Keys.FAVORITES] = current
        }
    }

    override suspend fun removeFavorite(id: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            current.remove(id.toString())
            prefs[Keys.FAVORITES] = current
        }
    }

    private companion object Keys {
        val FAVORITES = stringSetPreferencesKey("favorites")
    }
}
