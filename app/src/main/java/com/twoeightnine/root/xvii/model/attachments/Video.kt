package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by root on 10/14/16.
 */

@Parcelize
data class Video(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("owner_id")
        val ownerId: Int = 0,

        @SerializedName("duration")
        @Expose
        val duration: Int = 0,

        @SerializedName("title")
        @Expose
        val title: String? = null,

        @SerializedName("photo_130")
        @Expose
        val photo130: String? = null,

        @SerializedName("photo_320")
        @Expose
        val photo320: String? = null,

        @SerializedName("access_key")
        @Expose
        val accessKey: String? = null,

        @SerializedName("player")
        @Expose
        val player: String? = null,

        @SerializedName("link")
        @Expose
        val link: String? = null
) : Parcelable, IdTypeable {
    val maxPhoto: String
        get() = photo320 ?: photo130 ?: ""

    val videoId: String
        get() = "${ownerId}_$id"

    override fun getId() = "video$videoId"
}
