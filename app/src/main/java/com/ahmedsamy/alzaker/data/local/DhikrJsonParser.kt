package com.ahmedsamy.alzaker.data.local

import com.ahmedsamy.alzaker.data.model.DhikrItem
import org.json.JSONArray

/**
 * Parses the bundled JSON assets into domain models. Pure JVM (relies only on
 * org.json, which ships with the Android platform), so it is unit-testable.
 */
object DhikrJsonParser {

    /**
     * Parses the adhkar list. In the asset, "repeat" is a numeric string
     * (e.g. "3"), so it is read as text and converted defensively.
     */
    fun parseAdhkar(json: String): List<DhikrItem> {
        val array = JSONArray(json)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    DhikrItem(
                        id = obj.getInt("id"),
                        category = obj.optString("category", ""),
                        dhikr = obj.optString("dhikr", ""),
                        repeat = obj.optString("repeat", "1").toIntOrNull() ?: 1,
                        audioUrl = if (obj.isNull("audio_url")) null else obj.optString("audio_url"),
                    )
                )
            }
        }
    }

    /**
     * Parses a flat JSON array of strings (e.g. audio_drop_texts.json).
     */
    fun parseStringList(json: String): List<String> {
        val array = JSONArray(json)
        return buildList {
            for (i in 0 until array.length()) {
                add(array.getString(i))
            }
        }
    }
}
