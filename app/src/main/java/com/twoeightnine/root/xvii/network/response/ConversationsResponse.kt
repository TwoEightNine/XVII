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
import com.twoeightnine.root.xvii.model.DialogMessage
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
        dm.isGroup() -> getGroup(-(dm.lastMessage?.peerId ?: 0))?.name
        else -> getUser(dm.lastMessage?.peerId ?: 0)?.fullName
    }

    fun getPhotoFor(dm: DialogMessage) = when {
        dm.isChat() -> dm.conversation.chatSettings?.photo?.photo100
        dm.isGroup() -> getGroup(-(dm.lastMessage?.peerId ?: 0))?.photo100
        else -> getUser(dm.lastMessage?.peerId ?: 0)?.photo100
    }

    fun isOnline(dm: DialogMessage) = when {
        dm.isChat() || dm.isGroup() -> false
        else -> getUser(dm.lastMessage?.peerId ?: 0)?.isOnline ?: false
    }
}