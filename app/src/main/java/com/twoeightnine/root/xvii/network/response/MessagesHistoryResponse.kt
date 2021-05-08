package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.Conversation
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.utils.matchesChatId
import com.twoeightnine.root.xvii.utils.matchesGroupId
import com.twoeightnine.root.xvii.utils.matchesUserId

data class MessagesHistoryResponse(
        @SerializedName("count")
        @Expose
        val count: Int = 0,

        @SerializedName("items")
        @Expose
        val items: ArrayList<Message> = arrayListOf(),

        @SerializedName("profiles")
        @Expose
        val profiles: ArrayList<User>,

        @SerializedName("groups")
        @Expose
        val groups: ArrayList<Group>,

        @SerializedName("conversations")
        @Expose
        val conversations: ArrayList<Conversation>?
) {
    fun getProfileById(id: Int) = profiles.find { it.id == id }

    fun getGroupById(id: Int) = groups.find { it.id == id }

    fun getConversationById(id: Int) = conversations?.find { it.peer?.id == id }

    fun getNameForMessage(message: Message) = when {
        message.fromId.matchesUserId() -> getProfileById(message.fromId)?.fullName
        message.fromId.matchesGroupId() -> getGroupById(-message.fromId)?.name
        message.peerId.matchesChatId() -> getConversationById(message.peerId)?.chatSettings?.title
        else -> null
    }

    fun getPhotoForMessage(message: Message) = when {
        message.fromId.matchesUserId() -> getProfileById(message.fromId)?.photo100
        message.fromId.matchesGroupId() -> getGroupById(-message.fromId)?.photo100
        message.peerId.matchesChatId() -> getConversationById(message.peerId)?.chatSettings?.photo?.photo100
        else -> null
    }

    fun isMessageRead(message: Message) = message.isOut() && message.id <= getConversationById(message.peerId)?.outRead ?: 0
            || !message.isOut() && message.id <= getConversationById(message.peerId)?.inRead ?: 0
}