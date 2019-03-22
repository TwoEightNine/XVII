package com.twoeightnine.root.xvii.dialogs2.models.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.Message

data class DialogMessage(

        @SerializedName("conversation")
        @Expose
        val conversation: Conversation,

        @SerializedName("last_message")
        @Expose
        val lastMessage: Message
) {
    fun isChat() = lastMessage.peerId > 2000000000

    fun isGroup() = lastMessage.peerId < 0
}