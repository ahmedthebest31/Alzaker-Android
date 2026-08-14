package com.ahmedsamy.alzaker.data.model

/**
 * A single dhikr (remembrance) entry, mirroring the legacy adhkar.json schema.
 */
data class DhikrItem(
    val id: Int,
    val category: String,
    val dhikr: String,
    val repeat: Int,
    val audioUrl: String?,
)
