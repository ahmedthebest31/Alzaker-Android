package com.ahmedsamy.alzaker.util

import android.content.Context
import android.content.Intent

/**
 * System share helper, mirroring the legacy expo-sharing / React Native Share
 * usage. Returns true when the chooser was opened.
 */
object Share {

    fun shareText(context: Context, text: String): Boolean {
        return runCatching {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(
                Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            true
        }.getOrDefault(false)
    }
}
