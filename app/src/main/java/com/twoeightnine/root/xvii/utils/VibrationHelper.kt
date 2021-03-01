package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import global.msnthrp.xvii.uikit.utils.VersionUtils

object VibrationHelper {

    private const val HAPTIC = 15L

    private lateinit var vibrator: Vibrator

    fun vibrateHaptic() {
        if (VersionUtils.supportsVibrationEffects()) {
            vibrator.vibrate(VibrationEffect.createOneShot(HAPTIC, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(HAPTIC)
        }
    }

    fun initVibrator(context: Context) {
        if (!::vibrator.isInitialized) {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}