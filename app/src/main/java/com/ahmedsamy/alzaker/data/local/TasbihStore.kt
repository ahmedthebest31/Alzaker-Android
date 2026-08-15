package com.ahmedsamy.alzaker.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Shared source of truth for the tasbih counter count and goal.
 *
 * The tasbih tab screen and the home-screen tasbih widget both read and write
 * this store so that tapping either one is reflected in the other. The widget
 * broadcast receiver needs synchronous reads, so SharedPreferences is used
 * instead of the asynchronous DataStore. Changes are observable in-process via
 * [registerListener] (the widget receiver runs in the app process).
 */
interface TasbihStore {

    val count: Int

    val goal: Int

    fun setCount(value: Int)

    fun setGoal(value: Int)

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)
}

/**
 * [TasbihStore] backed by a dedicated SharedPreferences file. A goal of 0
 * means "no goal set" (no progress bar and no "N/M" label in the widget).
 */
class SharedPreferencesTasbihStore(context: Context) : TasbihStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override val count: Int
        get() = prefs.getInt(KEY_COUNT, 0)

    override val goal: Int
        get() = prefs.getInt(KEY_GOAL, 0)

    override fun setCount(value: Int) {
        prefs.edit().putInt(KEY_COUNT, value.coerceAtLeast(0)).apply()
    }

    override fun setGoal(value: Int) {
        prefs.edit().putInt(KEY_GOAL, value.coerceAtLeast(0)).apply()
    }

    override fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private companion object {
        const val PREFS_NAME = "tasbih_store"
        const val KEY_COUNT = "tasbih_count"
        const val KEY_GOAL = "tasbih_goal"
    }
}
