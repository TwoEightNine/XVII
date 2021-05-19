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

package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Sticker(

        @SerializedName("sticker_id")
        val stickerId: Int = 0,

        @SerializedName("product_id")
        @Expose
        val productId: Int = 0,

        @SerializedName("is_allowed")
        val isAllowed: Boolean = true,

        /**
         * manually added, not a field of server model
         */
        @SerializedName("keywords")
        val keywords: ArrayList<String> = arrayListOf()
) : Parcelable {

    override fun equals(other: Any?) =
            (other as? Sticker)?.stickerId == stickerId && stickerId != 0

    override fun hashCode() = stickerId

    val photo256: String
        get() = String.format(URL_256_FMT, stickerId)

    val photo512: String
        get() = String.format(URL_512_FMT, stickerId)

    companion object {

        const val URL_512_FMT = "https://vk.com/sticker/1-%d-512b"
        const val URL_256_FMT = "https://vk.com/sticker/1-%d-256b"
    }
}