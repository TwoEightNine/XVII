package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by root on 9/6/16.
 */

class LongPollResponse() : Serializable {

    @SerializedName("ts")
    var ts: Int = 0
    @SerializedName("updates")
    var updates: MutableList<MutableList<Any>>? = null
    @SerializedName("failed")
    val failed: Int = 0

    constructor(upds: MutableList<MutableList<Any>>, ts: Int): this() {
        updates = upds
        this.ts = ts
    }

    override fun toString() = "{ts: $ts, failed: $failed, updates: ${updates.toString()}}"

    fun toStringSafe() = "{ts: $ts, failed: $failed, updates: ${updates?.size}}"
}
