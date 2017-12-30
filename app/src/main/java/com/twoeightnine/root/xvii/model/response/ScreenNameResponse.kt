package com.twoeightnine.root.xvii.response

import com.google.gson.annotations.SerializedName

class ScreenNameResponse {

    val type: String? = null
    @SerializedName("object_id")
    val objectId: Int = 0

    val isUser: Boolean
        get() = USER == type

    companion object {

        val USER = "user"
    }
}
