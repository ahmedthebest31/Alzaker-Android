package com.ahmedsamy.alzaker.data.repository

import com.ahmedsamy.alzaker.data.model.DhikrItem
import kotlin.random.Random

/**
 * In-memory implementation fed by the parsed assets. Pure Kotlin, so it is
 * unit-testable without Android.
 */
class DhikrRepositoryImpl(
    dhikrList: List<DhikrItem>,
    audioDropTexts: List<String>,
) : DhikrRepository {

    override val allDhikr: List<DhikrItem> = dhikrList

    override val audioDropTexts: List<String> = audioDropTexts

    private val dhikrById: Map<Int, DhikrItem> = dhikrList.associateBy { it.id }

    override val categories: List<String> =
        listOf(DhikrRepository.ALL_CATEGORIES) + dhikrList.map { it.category }.distinct()

    override fun getDhikrById(id: Int): DhikrItem? = dhikrById[id]

    override fun getRandomDhikr(random: Random): DhikrItem? =
        if (allDhikr.isEmpty()) null else allDhikr[random.nextInt(allDhikr.size)]

    override fun getDhikrByCategory(category: String): List<DhikrItem> =
        if (category == DhikrRepository.ALL_CATEGORIES) {
            allDhikr
        } else {
            allDhikr.filter { it.category == category }
        }
}
