package com.twoeightnine.root.xvii.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by root on 10/14/16.
 */

@Parcelize
data class Link(

        @SerializedName("url")
        @Expose
        val url: String? = null,

        @SerializedName("title")
        @Expose
        val title: String? = null,

        @SerializedName("caption")
        @Expose
        val caption: String? = null,

        @SerializedName("description")
        @Expose
        val description: String? = null,

        @SerializedName("photo")
        @Expose
        val photo: Photo? = null
) : Parcelable
