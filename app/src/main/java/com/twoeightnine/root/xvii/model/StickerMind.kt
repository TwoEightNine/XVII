package com.twoeightnine.root.xvii.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StickerMind(

        @SerializedName("user_stickers")
        @Expose
        val userStickers: MutableList<Int>? = null
) : Parcelable