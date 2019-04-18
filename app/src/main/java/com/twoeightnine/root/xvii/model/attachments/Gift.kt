package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Gift(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("thumb_256")
        @Expose
        val thumb256: String? = null
) : Parcelable