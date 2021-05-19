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

package com.twoeightnine.root.xvii.background.longpoll.models.events

import android.content.res.Resources
import com.twoeightnine.root.xvii.R

data class OnlineEvent(
        val userId: Int,
        val extras: Int,
        val timeStamp: Int
) : BaseLongPollEvent() {

    override fun getType() = TYPE_ONLINE

    val deviceCode: Int
        get() = extras and 0xff

    companion object {

        fun getDeviceName(resources: Resources?, deviceCode: Int): String {
            if (resources == null || deviceCode !in 1..7) return ""

            return resources.getStringArray(R.array.devices)[deviceCode - 1]
        }

    }

}