package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by root on 9/2/16.
 */
@Parcelize
data class Photo(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("album_id")
        @Expose
        val albumId: Int = 0,

        @SerializedName("owner_id")
        @Expose
        val ownerId: Int = 0,

        @SerializedName("photo_75")
        @Expose
        val photo75: String? = null,

        @SerializedName("photo_130")
        @Expose
        val photo130: String? = null,

        @SerializedName("photo_604")
        @Expose
        val photo604: String? = null,

        @SerializedName("photo_807")
        @Expose
        val photo807: String? = null,

        @SerializedName("photo_1280")
        @Expose
        val photo1280: String? = null,

        @SerializedName("photo_2560")
        @Expose
        val photo2560: String? = null,

        @SerializedName("width")
        @Expose
        val width: Int = 0,

        @SerializedName("height")
        @Expose
        val height: Int = 0,

        @SerializedName("text")
        @Expose
        val text: String? = null,

        @SerializedName("date")
        @Expose
        val date: Int = 0,

        @SerializedName("post_id")
        @Expose
        val postId: Int = 0,

        @SerializedName("access_key")
        @Expose
        var accessKey: String = ""
) : Parcelable, IdTypeable {

    val maxPhoto: String
        get() {
            var max = almostMax
            if (photo2560 != null) max = photo2560
            return max
        }

    val almostMax: String
        get() {
            var max = optimalPhoto
            if (photo807 != null) max = photo807
            if (photo1280 != null) max = photo1280
            return max
        }

    val optimalPhoto: String
        get() {
            var max = smallPhoto
            if (photo604 != null) max = photo604
            return max
        }

    val smallPhoto: String
        get() {
            var max = photo75
            if (photo130 != null) max = photo130
            return max ?: ""
        }

    val ratio: Double
        get() = width.toDouble() / height

    val photoId: String
        get() = "${ownerId}_$id"

    override fun getId() = "photo$photoId"
}
