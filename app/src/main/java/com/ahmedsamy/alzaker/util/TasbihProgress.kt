package com.ahmedsamy.alzaker.util

/**
 * Pure helpers shared by the tasbih tab screen and the tasbih widget so both
 * show identical goal-reached state and the compact "N/M" progress label.
 */
object TasbihProgress {

    /** True when a goal is set and the count has reached or passed it. */
    fun isGoalReached(count: Int, goal: Int): Boolean = goal > 0 && count >= goal

    /**
     * Compact progress label shown on the widget ("3/33"). Returns null when
     * no goal is set so the widget hides the label and the progress bar.
     */
    fun progressLabel(count: Int, goal: Int): String? = if (goal > 0) "$count/$goal" else null

    /** Screen-reader friendly description of the counter state. */
    fun contentDescription(count: Int, goal: Int): String = if (goal > 0) "العداد: $count من $goal" else "العداد: $count"
}
