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

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.StickerMind
import com.twoeightnine.root.xvii.model.attachments.Sticker
import kotlinx.android.parcel.Parcelize

/**
 * Created by root on 3/24/17.
 */

@Parcelize
data class StickersResponse(

        @SerializedName("base_url")
        val baseUrl: String? = null,

        @SerializedName("count")
        val count: Int = 0,

        @SerializedName("dictionary")
        val dictionary: MutableList<StickerMind>? = null
) : Parcelable {

    fun getStickerIdToWordsMap(): Map<Int, List<String>> {
        val result = hashMapOf<Int, List<String>>()
        dictionary?.forEach { mind ->
            mind.userStickers?.forEach { sticker ->
                if (sticker.isAllowed && mind.words != null) {
                    result[sticker.stickerId] = mind.words
                }
            }
        }
        return result
    }

    fun getStickers(): List<Sticker> {
        val stickers = arrayListOf<Sticker>()
        dictionary?.forEach { mind ->
            mind.userStickers?.forEach { sticker ->
                if (sticker.isAllowed) {
                    mind.words?.filter { it.isNotBlank() }?.also { words ->
                        sticker.keywords.addAll(words)
                    }
                    stickers.add(sticker)
                }
            }
        }
        val preparedStickers = ArrayList(stickers.sortedBy { it.stickerId }.distinctBy { it.stickerId })
        val toAdd = arrayListOf<Int>()
        for (i in preparedStickers.indices) {
            if (i == 0) continue

            val diff = preparedStickers[i].stickerId - preparedStickers[i - 1].stickerId
            if (diff != 1 && diff <= ALLOWED_DIFF) {
                val from = preparedStickers[i - 1].stickerId + 1
                val to = preparedStickers[i].stickerId - 1
                for (j in from..to) {
                    toAdd.add(j)
                }
            }
        }
        for (stickerId in toAdd) {
            preparedStickers.add(Sticker(stickerId))
        }
        return ArrayList(preparedStickers.sortedBy { it.stickerId }.distinctBy { it.stickerId })
    }

    companion object {

        const val ALLOWED_DIFF = 7
    }
}
