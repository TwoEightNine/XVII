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

package global.msnthrp.xvii.uikit.utils

import android.app.Activity
import android.util.DisplayMetrics
import android.view.WindowManager

object DisplayUtils {

    private lateinit var windowManager: WindowManager

    val screenWidth by lazy {
        DisplayMetrics().let {
            windowManager.defaultDisplay.getMetrics(it)
            it.widthPixels
        }
    }

    val screenHeight by lazy {
        DisplayMetrics().let {
            windowManager.defaultDisplay.getMetrics(it)
            it.heightPixels
        }
    }

    fun initIfNot(activity: Activity) {
        if (!::windowManager.isInitialized) {
            windowManager = activity.windowManager
        }
    }

}