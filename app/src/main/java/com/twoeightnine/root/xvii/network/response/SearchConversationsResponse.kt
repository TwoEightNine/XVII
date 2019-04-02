package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.Conversation
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.matchesChatId
import com.twoeightnine.root.xvii.utils.matchesGroupId
import com.twoeightnine.root.xvii.utils.matchesUserId

data class SearchConversationsResponse(

        @SerializedName("items")
        @Expose
        val items: ArrayList<Conversation> = arrayListOf(),

        @SerializedName("profiles")
        @Expose
        val profiles: ArrayList<User> = arrayListOf(),

        @SerializedName("groups")
        @Expose
        val groups: ArrayList<Group> = arrayListOf()
) {
    fun getUser(userId: Int) = profiles.find { it.id == userId }

    fun getGroup(groupId: Int) = groups.find { it.id == groupId }

    fun getTitleFor(conversation: Conversation): String? {
        val peerId = conversation.peer?.id ?: return null
        return when {
            peerId.matchesChatId() -> conversation.chatSettings?.title
            peerId.matchesGroupId() -> getGroup(-peerId)?.name
            else -> getUser(peerId)?.fullName
        }
    }

    fun getPhotoFor(conversation: Conversation): String? {
        val peerId = conversation.peer?.id ?: return null
        return when {
            peerId.matchesChatId() -> conversation.chatSettings?.photo?.photo100
            peerId.matchesGroupId() -> getGroup(-peerId)?.photo100
            else -> getUser(peerId)?.photo100
        }
    }

    fun isOnline(conversation: Conversation): Boolean {
        val peerId = conversation.peer?.id ?: return false
        return when {
            peerId.matchesUserId() -> getUser(peerId)?.isOnline == true
            else -> false
        }
    }
}