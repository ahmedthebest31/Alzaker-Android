package com.ahmedsamy.alzaker.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persists and exposes the set of favorite dhikr ids.
 */
interface FavoritesRepository {

    val favoriteIds: Flow<Set<Int>>

    suspend fun isFavorite(id: Int): Boolean

    /**
     * Adds or removes [id] and returns the new state: true if now favorite,
     * false if it was removed (mirrors the legacy toggleFavorite).
     */
    suspend fun toggleFavorite(id: Int): Boolean

    suspend fun addFavorite(id: Int)

    suspend fun removeFavorite(id: Int)
}
