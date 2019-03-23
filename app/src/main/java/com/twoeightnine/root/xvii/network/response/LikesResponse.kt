package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by root on 12/31/16.
 */

data class LikesResponse(
        @SerializedName("likes")
        @Expose
        val likes: Int = 0
)
