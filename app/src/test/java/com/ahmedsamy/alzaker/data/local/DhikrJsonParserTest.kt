package com.ahmedsamy.alzaker.data.local

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DhikrJsonParserTest {

    @Test
    fun parseAdhkar_sampleItem() {
        val json = """
            [
              {
                "category": "أذكار الصباح",
                "dhikr": "بسم الله",
                "repeat": "3",
                "audio_url": "https://example.com/1.mp3",
                "id": 1
              }
            ]
        """.trimIndent()

        val items = DhikrJsonParser.parseAdhkar(json)

        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(1, item.id)
        assertEquals("أذكار الصباح", item.category)
        assertEquals("بسم الله", item.dhikr)
        assertEquals(3, item.repeat)
        assertEquals("https://example.com/1.mp3", item.audioUrl)
    }

    @Test
    fun parseAdhkar_realAssetAllFieldsValid() {
        val file = File("src/main/assets/adhkar.json")
        assertTrue("adhkar.json asset must exist", file.exists())

        val items = DhikrJsonParser.parseAdhkar(file.readText(Charsets.UTF_8))

        assertEquals(267, items.size)
        assertEquals(267, items.map { it.id }.distinct().size)
        assertTrue("ids must be positive", items.all { it.id > 0 })
        assertTrue("categories must be non-blank", items.all { it.category.isNotBlank() })
        assertTrue("dhikr texts must be non-blank", items.all { it.dhikr.isNotBlank() })
        assertTrue("repeat must be positive", items.all { it.repeat > 0 })
        assertTrue("every item must carry an audio_url", items.all { !it.audioUrl.isNullOrEmpty() })
    }

    @Test
    fun parseAdhkar_emptyArray() {
        assertTrue(DhikrJsonParser.parseAdhkar("[]").isEmpty())
    }

    @Test
    fun parseAdhkar_missingAudioUrlYieldsNull() {
        val json = """[{"id": 5, "category": "أذكار", "dhikr": "سبحان الله", "repeat": "1"}]"""
        val item = DhikrJsonParser.parseAdhkar(json).first()
        assertEquals(5, item.id)
        assertEquals(null, item.audioUrl)
    }

    @Test
    fun parseStringList_realAsset() {
        val file = File("src/main/assets/audio_drop_texts.json")
        assertTrue("audio_drop_texts.json asset must exist", file.exists())

        val texts = DhikrJsonParser.parseStringList(file.readText(Charsets.UTF_8))

        assertEquals(97, texts.size)
        assertTrue(texts.all { it.isNotBlank() })
    }
}
