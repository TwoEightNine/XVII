package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.utils.matchesChatId
import com.twoeightnine.root.xvii.utils.matchesGroupId

data class DialogMessage(

        @SerializedName("conversation")
        @Expose
        val conversation: Conversation,

        @SerializedName("last_message")
        @Expose
        val lastMessage: Message
) {
    fun isChat() = lastMessage.peerId.matchesChatId()

    fun isGroup() = lastMessage.peerId.matchesGroupId()
}