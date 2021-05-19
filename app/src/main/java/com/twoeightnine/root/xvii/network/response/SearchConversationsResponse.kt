/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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