package com.twoeightnine.root.xvii.network.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.lg.Lg
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

    fun getStickers(): List<Sticker> {
        val stickers = arrayListOf<Sticker>()
        dictionary?.forEach { mind ->
            mind.userStickers?.forEach {
                if (it.isAllowed) {
                    stickers.add(it)
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
