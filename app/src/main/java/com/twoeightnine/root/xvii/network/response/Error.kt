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

package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R


data class Error(

        @SerializedName("error_code")
        val code: Int = 0,
        @SerializedName("error_msg")
        val message: String? = null,
        @SerializedName("captcha_sid")
        val captchaSid: String? = null,
        @SerializedName("captcha_img")
        val captchaImg: String? = null
) {

    fun friendlyMessage() =
            when (code) {
                1 -> App.context.getString(R.string.error_1)
                5 -> App.context.getString(R.string.error_5)
                6 -> App.context.getString(R.string.error_6)
                7 -> App.context.getString(R.string.error_7)
                9 -> App.context.getString(R.string.error_9)
                10 -> App.context.getString(R.string.error_10)
                14 -> App.context.getString(R.string.error_14)
                15 -> App.context.getString(R.string.error_15)
                17 -> App.context.getString(R.string.error_17)
                200, 201, 203 -> App.context.getString(R.string.error_200plus)
                500, 600, 603 -> App.context.getString(R.string.error_500plus)
                else -> message
            }

    companion object {
        const val AUTH_FAILED = 5
        const val TOO_MANY = 6
        const val CAPTCHA = 14
    }
}