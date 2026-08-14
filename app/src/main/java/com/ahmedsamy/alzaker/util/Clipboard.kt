package com.ahmedsamy.alzaker.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Clipboard helper, mirroring the legacy expo-clipboard setStringAsync.
 */
object Clipboard {

    fun copyText(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
