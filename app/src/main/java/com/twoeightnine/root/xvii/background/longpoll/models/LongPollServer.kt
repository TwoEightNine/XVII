package com.twoeightnine.root.xvii.background.longpoll.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LongPollServer(

    @SerializedName("key")
    @Expose
    val key: String,

    @SerializedName("server")
    @Expose
    val server: String,

    @SerializedName("ts")
    @Expose
    val ts: Int
) : Parcelable