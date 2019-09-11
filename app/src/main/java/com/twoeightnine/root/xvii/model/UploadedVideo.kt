package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by root on 10/14/16.
 */

data class UploadedVideo(

        @SerializedName("owner_id")
        @Expose
        val ownerId: Int = 0,

        @SerializedName("video_id")
        @Expose
        val videoId: Int = 0,

        @SerializedName("video_hash")
        val videoHash: String = ""
)
