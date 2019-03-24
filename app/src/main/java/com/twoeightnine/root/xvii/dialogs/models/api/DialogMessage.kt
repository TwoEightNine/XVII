package com.twoeightnine.root.xvii.dialogs.models.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DialogMessage(

        @SerializedName("conversation")
        @Expose
        val conversation: Conversation,

        @SerializedName("last_message")
        @Expose
        val lastMessage: Message2
) {
    fun isChat() = lastMessage.peerId > 2000000000

    fun isGroup() = lastMessage.peerId < 0
}