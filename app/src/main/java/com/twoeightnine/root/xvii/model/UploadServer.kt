package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by root on 10/12/16.
 */

data class UploadServer(
        @SerializedName("upload_url")
        @Expose
        val uploadUrl: String? = null,

        @SerializedName("album_id")
        @Expose
        val albumId: Int = 0,

        @SerializedName("user_id")
        @Expose
        val userId: Int = 0
)
