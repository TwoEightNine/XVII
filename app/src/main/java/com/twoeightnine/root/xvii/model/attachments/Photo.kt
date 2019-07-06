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

        @SerializedName("sizes")
        @Expose
        val sizes: ArrayList<PhotoSize> = arrayListOf(),

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

    val ratio: Double
        get() = width.toDouble() / height

    val photoId: String
        get() = "${ownerId}_$id"

    override fun getId() = "photo$photoId"

    /**
     * removes unnecessary sizes, keeps only [types]
     * sorts by size descending
     */
    private fun filteredSizes() = sizes.filter { it.type in types }
            .sortedByDescending { types.indexOf(it.type) }

    /**
     * keeps only sizes that are smaller that [type]
     */
    private fun filteredByType(type: String) = filteredSizes()
            .filter { types.indexOf(it.type) <= types.indexOf(type) }

    fun getOptimalPhoto() = filteredByType(TYPE_Y).first()

    fun getLargePhoto() = filteredByType(TYPE_Z).first()

    fun getMaxPhoto() = filteredByType(TYPE_W).first()

    fun getSmallPhoto() = filteredByType(TYPE_M).first()

    fun getMediumPhoto() = filteredByType(TYPE_P).first()

    companion object {

        const val TYPE_S = "s"
        const val TYPE_M = "m"
        const val TYPE_P = "p"
        const val TYPE_Q = "q"
        const val TYPE_X = "x"
        const val TYPE_Y = "y"
        const val TYPE_Z = "z"
        const val TYPE_W = "w"

        val types = arrayListOf(
                TYPE_S,
                TYPE_M,
                TYPE_P,
                TYPE_Q,
                TYPE_X,
                TYPE_Y,
                TYPE_Z,
                TYPE_W
        )
    }
}

@Parcelize
data class PhotoSize(

        @SerializedName("type")
        @Expose
        val type: String,

        @SerializedName("url")
        @Expose
        val url: String,

        @SerializedName("width")
        @Expose
        val width: Int,

        @SerializedName("height")
        @Expose
        val height: Int
) : Parcelable
