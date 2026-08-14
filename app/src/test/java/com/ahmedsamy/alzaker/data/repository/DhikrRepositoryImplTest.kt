package com.ahmedsamy.alzaker.data.repository

import com.ahmedsamy.alzaker.data.model.DhikrItem
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DhikrRepositoryImplTest {

    private val items = listOf(
        DhikrItem(id = 1, category = "أذكار الصباح", dhikr = "أولا", repeat = 1, audioUrl = null),
        DhikrItem(id = 2, category = "أذكار الصباح", dhikr = "ثانيا", repeat = 3, audioUrl = "u2"),
        DhikrItem(id = 3, category = "أذكار المساء", dhikr = "ثالثا", repeat = 10, audioUrl = "u3"),
    )

    private val repository = DhikrRepositoryImpl(
        dhikrList = items,
        audioDropTexts = listOf("نص 1", "نص 2"),
    )

    @Test
    fun categoriesArePrefixedWithAllSentinel() {
        assertEquals(listOf(DhikrRepository.ALL_CATEGORIES, "أذكار الصباح", "أذكار المساء"), repository.categories)
    }

    @Test
    fun getDhikrByCategoryAllReturnsEverything() {
        assertEquals(3, repository.getDhikrByCategory(DhikrRepository.ALL_CATEGORIES).size)
    }

    @Test
    fun getDhikrByCategoryFilters() {
        val morning = repository.getDhikrByCategory("أذكار الصباح")
        assertEquals(listOf(1, 2), morning.map { it.id })
    }

    @Test
    fun getDhikrByIdKnownAndUnknown() {
        assertNotNull(repository.getDhikrById(2))
        assertEquals("ثانيا", repository.getDhikrById(2)?.dhikr)
        assertNull(repository.getDhikrById(999))
    }

    @Test
    fun getRandomDhikrSeededRandomStaysInRange() {
        val random = Random(42)
        repeat(100) {
            val picked = repository.getRandomDhikr(random)
            assertNotNull(picked)
            assertTrue(items.any { it.id == picked!!.id })
        }
    }

    @Test
    fun getRandomDhikrEmptyListReturnsNull() {
        val empty = DhikrRepositoryImpl(emptyList(), emptyList())
        assertNull(empty.getRandomDhikr())
    }

    @Test
    fun audioDropTextsAreExposed() {
        assertEquals(listOf("نص 1", "نص 2"), repository.audioDropTexts)
    }
}
