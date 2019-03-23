package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * Created by root on 9/1/16.
 */

data class BaseResponse<T>(

        @SerializedName("response")
        @Expose
        val response: T? = null,

        @SerializedName("error")
        @Expose
        val error: Error? = null
)
