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

package com.twoeightnine.root.xvii.model

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.utils.matchesChatId
import com.twoeightnine.root.xvii.utils.matchesGroupId

data class DialogMessage(

        @SerializedName("conversation")
        @Expose
        val conversation: Conversation,

        @SerializedName("last_message")
        @Expose
        private val lastMessageInternal: Any? // Message or Boolean. VK, WTF??
) {

    var lastMessage: Message? = null
        get() {
            if (field == null && lastMessageInternal is LinkedTreeMap<*, *>) {
                field = tryToCastMessage()
            }
            return field
        }
        private set


    fun isChat() = lastMessage?.peerId?.matchesChatId() == true

    fun isGroup() = lastMessage?.peerId?.matchesGroupId() == true

    private fun tryToCastMessage(): Message? =
            try {
                val gson = Gson()
                val json = gson.toJson(lastMessageInternal)
                gson.fromJson(json, Message::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

}