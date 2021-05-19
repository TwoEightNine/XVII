/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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