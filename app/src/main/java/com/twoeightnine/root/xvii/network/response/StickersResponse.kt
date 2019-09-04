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

    fun getStickers(): List<Sticker> {
        val stickers = arrayListOf<Sticker>()
        dictionary?.forEach { mind ->
            mind.userStickers?.forEach {
                stickers.add(Sticker(it))
            }
        }
        return ArrayList(stickers.sortedBy { it.id }.distinctBy { it.id })
    }
}
