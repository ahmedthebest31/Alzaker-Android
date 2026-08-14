package com.ahmedsamy.alzaker.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * The haptic feedback styles available in the app, mirroring the legacy
 * HapticFeedbackType enum in utils/haptics.ts.
 */
enum class HapticFeedbackType {
    ImpactLight,
    ImpactMedium,
    ImpactHeavy,
    NotificationSuccess,
    NotificationWarning,
    NotificationError,
}

/**
 * Central gateway for all haptic feedback, mirroring the legacy triggerHaptic.
 * Does nothing when [enabled] is false or the device has no vibrator.
 */
object Haptics {

    fun trigger(context: Context, type: HapticFeedbackType, enabled: Boolean) {
        if (!enabled) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) return

        val effect: VibrationEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val preset = when (type) {
                HapticFeedbackType.ImpactLight -> VibrationEffect.EFFECT_CLICK
                HapticFeedbackType.ImpactMedium -> VibrationEffect.EFFECT_DOUBLE_CLICK
                HapticFeedbackType.ImpactHeavy -> VibrationEffect.EFFECT_HEAVY_CLICK
                HapticFeedbackType.NotificationSuccess -> VibrationEffect.EFFECT_DOUBLE_CLICK
                HapticFeedbackType.NotificationWarning -> VibrationEffect.EFFECT_HEAVY_CLICK
                HapticFeedbackType.NotificationError -> VibrationEffect.EFFECT_HEAVY_CLICK
            }
            VibrationEffect.createPredefined(preset)
        } else {
            VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    }
}
