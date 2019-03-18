package com.twoeightnine.root.xvii.background.longpoll.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LongPollUpdate(

    @SerializedName("ts")
    @Expose
    val ts: Int = 0,

    @SerializedName("updates")
    @Expose
    val updates: ArrayList<ArrayList<Any>> = arrayListOf(),

    @SerializedName("failed")
    @Expose
    val failed: Int = 0
) {
    fun shouldUpdateServer() = failed == 2 || failed == 3

    fun shouldUpdateTs() = failed == 1
}