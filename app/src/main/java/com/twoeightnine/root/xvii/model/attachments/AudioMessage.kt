package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioMessage(

        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("owner_id")
        @Expose
        val ownerId: Int,

        @SerializedName("duration")
        @Expose
        val duration: Int,

        @SerializedName("link_mp3")
        @Expose
        val linkMp3: String,

        @SerializedName("access_key")
        @Expose
        val accessKey: String
): Parcelable