package com.ahmedsamy.alzaker.reminder

/**
 * Schedules reminder alarms as one-shot exact alarms. Each fired alarm
 * re-schedules the next one inside [ReminderReceiver] (self-rescheduling chain,
 * decision D6), so cancelling the single pending intent ends the whole chain.
 */
interface AlarmScheduler {

    /** Schedules the next one-shot alarm of [type] [intervalMinutes] from now. */
    fun scheduleNext(type: ReminderType, intervalMinutes: Int)

    /** Cancels the pending alarm of [type], ending its self-rescheduling chain. */
    fun cancel(type: ReminderType)
}
