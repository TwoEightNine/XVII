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

package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.utils.FileStorage

class StickersStorage(
        context: Context,
        type: Type
) : FileStorage<ArrayList<Sticker>>(context, type.name + "_v3") {

    private val gson = Gson()

    override fun serialize(data: ArrayList<Sticker>): String = gson.toJson(StickerWrapper(data))

    override fun deserialize(s: String) = try {
        gson.fromJson(s, StickerWrapper::class.java).stickers
    } catch (e: Exception) {
        e.printStackTrace()
        arrayListOf()
    }

    enum class Type {
        AVAILABLE,
        RECENT
    }

    data class StickerWrapper(
            @SerializedName("stickers_list")
            val stickers: ArrayList<Sticker>
    )
}