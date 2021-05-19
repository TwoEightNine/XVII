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

package com.twoeightnine.root.xvii.background.music.models

import android.os.Parcelable
import com.twoeightnine.root.xvii.model.attachments.Audio
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track(
        val audio: Audio,
        val cachePath: String? = null
) : Parcelable {

    fun isCached() = !cachePath.isNullOrEmpty()

    override fun equals(other: Any?) = audio == (other as? Track)?.audio

    override fun hashCode(): Int {
        var result = audio.hashCode()
        result = 31 * result + (cachePath?.hashCode() ?: 0)
        return result
    }
}