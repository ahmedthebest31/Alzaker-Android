package com.ahmedsamy.alzaker.data.local

import android.content.Context
import com.ahmedsamy.alzaker.data.model.DhikrItem

/**
 * Reads the bundled JSON assets. The parse is synchronous and cheap (a single
 * one-time pass); the repository caches the result.
 */
class LocalDhikrDataSource(context: Context) {

    private val assets = context.applicationContext.assets

    fun loadDhikrList(): List<DhikrItem> {
        val json = assets.open(ADHKAR_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return DhikrJsonParser.parseAdhkar(json)
    }

    fun loadAudioDropTexts(): List<String> {
        val json = assets.open(AUDIO_DROP_TEXTS_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return DhikrJsonParser.parseStringList(json)
    }

    companion object {
        const val ADHKAR_ASSET = "adhkar.json"
        const val AUDIO_DROP_TEXTS_ASSET = "audio_drop_texts.json"
    }
}
