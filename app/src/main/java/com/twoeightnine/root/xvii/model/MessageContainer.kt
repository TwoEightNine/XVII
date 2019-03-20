package com.twoeightnine.root.xvii.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageContainer(

        @SerializedName("message")
        @Expose
        val message: Message? = null,

        @SerializedName("unread")
        @Expose
        val unread: Int = 0
) : Parcelable