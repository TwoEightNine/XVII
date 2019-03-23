package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.dialogs2.models.api.DialogMessage
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User

data class ConversationsResponse(

        @SerializedName("items")
        @Expose
        val items: ArrayList<DialogMessage> = arrayListOf(),

        @SerializedName("count")
        @Expose
        val count: Long = 0L,

        @SerializedName("unread_count")
        @Expose
        val unreadCount: Int = 0,

        @SerializedName("profiles")
        @Expose
        val profiles: ArrayList<User> = arrayListOf(),

        @SerializedName("groups")
        @Expose
        val groups: ArrayList<Group> = arrayListOf()
) {
    fun getUser(userId: Int) = profiles.find { it.id == userId }

    fun getGroup(groupId: Int) = groups.find { it.id == groupId }

    fun getTitleFor(dm: DialogMessage) = when {
        dm.isChat() -> dm.conversation.chatSettings?.title
        dm.isGroup() -> getGroup(-dm.lastMessage.peerId)?.name
        else -> getUser(dm.lastMessage.peerId)?.fullName
    }

    fun getPhotoFor(dm: DialogMessage) = when {
        dm.isChat() -> dm.conversation.chatSettings?.photo?.photo100
        dm.isGroup() -> getGroup(-dm.lastMessage.peerId)?.photo100
        else -> getUser(dm.lastMessage.peerId)?.photo100
    }

    fun isOnline(dm: DialogMessage) = when {
        dm.isChat() || dm.isGroup() -> false
        else -> getUser(dm.lastMessage.peerId)?.isOnline ?: false
    }
}