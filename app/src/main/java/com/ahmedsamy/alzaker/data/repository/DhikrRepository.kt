package com.ahmedsamy.alzaker.data.repository

import com.ahmedsamy.alzaker.data.model.DhikrItem
import kotlin.random.Random

/**
 * Read-only access to the bundled adhkar content.
 */
interface DhikrRepository {

    val allDhikr: List<DhikrItem>

    /**
     * Wisdom/hikmah texts bundled alongside the audio drops
     * (assets/audio_drop_texts.json).
     */
    val audioDropTexts: List<String>

    /**
     * Distinct categories, prefixed with the "All" sentinel, mirroring the
     * legacy adhkar screen order: [ALL_CATEGORIES, ...categories].
     */
    val categories: List<String>

    fun getDhikrById(id: Int): DhikrItem?

    /**
     * Uniform random pick over [allDhikr], or null when the list is empty.
     * [random] is injectable for deterministic tests.
     */
    fun getRandomDhikr(random: Random = Random.Default): DhikrItem?

    fun getDhikrByCategory(category: String): List<DhikrItem>

    companion object {
        const val ALL_CATEGORIES = "الكل"
    }
}
