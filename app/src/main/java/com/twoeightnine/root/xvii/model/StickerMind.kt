package com.twoeightnine.root.xvii.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.attachments.Sticker
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StickerMind(

        @SerializedName("user_stickers")
        @Expose
        val userStickers: MutableList<Sticker>? = null,

        @SerializedName("words")
        val words: List<String>? = null
) : Parcelable