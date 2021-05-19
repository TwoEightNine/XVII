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

package com.twoeightnine.root.xvii.chats.attachments.gallery.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceItem(

        /**
         * modification date
         * the field these elements are sorted by
         */
        val date: Long,

        /**
         * path to file
         */
        val path: String,

        /**
         * type, photo, doc or video
         */
        val type: Type,

        /**
         * duration of video
         */
        val duration: Long = 0L,

        /**
         * path to thumbnail
         */
        var thumbnail: String? = null
) : Parcelable {

    enum class Type {
        PHOTO,
        VIDEO,
        DOC
    }
}