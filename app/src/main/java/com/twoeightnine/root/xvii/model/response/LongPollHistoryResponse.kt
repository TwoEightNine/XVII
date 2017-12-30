package com.twoeightnine.root.xvii.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.response.ListResponse
import java.io.Serializable

class LongPollHistoryResponse: Serializable {

    @SerializedName("history")
    @Expose
    val history: MutableList<MutableList<Any>>? = null

    @SerializedName("messages")
    @Expose
    val messages: ListResponse<Message>? = null

    override fun toString() = "history: ${history.toString()}"

    fun toStringSafe() = "history: ${history?.size}"
}