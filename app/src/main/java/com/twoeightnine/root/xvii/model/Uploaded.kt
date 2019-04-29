package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by root on 10/14/16.
 */

data class Uploaded(

        @SerializedName("server")
        @Expose
        val server: Int = 0,

        @SerializedName("photo")
        @Expose
        val photo: String? = null,

        @SerializedName("hash")
        @Expose
        val hash: String? = null,

        @SerializedName("file")
        @Expose
        val file: String? = null
)
