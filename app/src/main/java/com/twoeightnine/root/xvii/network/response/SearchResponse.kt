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
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.utils.matchesChatId
import com.twoeightnine.root.xvii.utils.matchesGroupId
import com.twoeightnine.root.xvii.utils.matchesUserId

data class SearchResponse(

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
    val conversations: ArrayList<Conversation>
) {
    fun getProfileById(id: Int) = profiles.find { it.id == id }

    fun getGroupById(id: Int) = groups.find { it.id == id }

    fun getConversationById(id: Int) = conversations.find { it.peer?.id == id }

    fun getTitleFor(message: Message) = when {
        message.peerId.matchesUserId() -> getProfileById(message.peerId)?.fullName
        message.peerId.matchesGroupId() -> getGroupById(-message.peerId)?.name
        message.peerId.matchesChatId() -> getConversationById(message.peerId)?.chatSettings?.title
        else -> null
    }

    fun getPhotoFor(message: Message) = when {
        message.peerId.matchesUserId() -> getProfileById(message.peerId)?.photo100
        message.peerId.matchesGroupId() -> getGroupById(-message.peerId)?.photo100
        message.peerId.matchesChatId() -> getConversationById(message.peerId)?.chatSettings?.photo?.photo100
        else -> null
    }


    fun isOnline(message: Message): Boolean {
        return when {
            message.peerId.matchesUserId() -> getProfileById(message.fromId)?.isOnline == true
            else -> false
        }
    }
}