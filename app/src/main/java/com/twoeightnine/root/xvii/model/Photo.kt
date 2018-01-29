package com.twoeightnine.root.xvii.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by root on 9/2/16.
 */

class Photo : Parcelable, Serializable {

    var id: Int? = null
    @SerializedName("album_id")
    var albumId: Int? = null
    @SerializedName("owner_id")
    var ownerId: Int? = null
    @SerializedName("photo_75")
    var photo75: String? = null
    @SerializedName("photo_130")
    var photo130: String? = null
    @SerializedName("photo_604")
    var photo604: String? = null
    @SerializedName("photo_807")
    var photo807: String? = null
    @SerializedName("photo_1280")
    var photo1280: String? = null
    @SerializedName("photo_2560")
    var photo2560: String? = null
    var width: Int? = null
    var height: Int? = null
    var text: String? = null
    var date: Int? = null
    @SerializedName("post_id")
    var postId: Int? = null
    @SerializedName("access_key")
    var accessKey: String? = null


    constructor(p: Parcel) {
        id = p.readInt()
        albumId = p.readInt()
        ownerId = p.readInt()
        photo75 = p.readString()
        photo130 = p.readString()
        photo604 = p.readString()
        photo807 = p.readString()
        photo1280 = p.readString()
        photo2560 = p.readString()
        width = p.readInt()
        height = p.readInt()
        text = p.readString()
        date = p.readInt()
        postId = p.readInt()
        accessKey = p.readString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id ?: 0)
        parcel.writeInt(albumId ?: 0)
        parcel.writeInt(ownerId ?: 0)
        parcel.writeString(photo75)
        parcel.writeString(photo130)
        parcel.writeString(photo604)
        parcel.writeString(photo807)
        parcel.writeString(photo1280)
        parcel.writeString(photo2560)
        parcel.writeInt(width ?: 0)
        parcel.writeInt(height ?: 0)
        parcel.writeString(text)
        parcel.writeInt(date ?: 0)
        parcel.writeInt(postId ?: 0)
        parcel.writeString(accessKey)
    }

    val maxPhoto: String
        get() {
            var max = almostMax
            if (photo2560 != null) max = photo2560!!
            return max
        }

    val almostMax: String
        get() {
            var max = optimalPhoto
            if (photo807 != null) max = photo807!!
            if (photo1280 != null) max = photo1280!!
            return max
        }

    val optimalPhoto: String
        get() {
            var max = photo75
            if (photo130 != null) max = photo130
            if (photo604 != null) max = photo604
            return max ?: ""
        }

    val photoId: String
        get() = "${ownerId}_$id"

    companion object {

        @JvmField @Suppress("unused")
        val CREATOR: Parcelable.Creator<Photo> = object : Parcelable.Creator<Photo> {
            override fun createFromParcel(parcel: Parcel) = Photo(parcel)

            override fun newArray(i: Int) = arrayOfNulls<Photo>(i)
        }
    }
}
