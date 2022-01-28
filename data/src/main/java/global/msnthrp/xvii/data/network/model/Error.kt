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

package global.msnthrp.xvii.data.network.model

import com.google.gson.annotations.SerializedName


data class Error(

        @SerializedName("error_code")
        val code: Int = 0,
        @SerializedName("error_msg")
        val message: String? = null,
        @SerializedName("captcha_sid")
        val captchaSid: String? = null,
        @SerializedName("captcha_img")
        val captchaImg: String? = null
)