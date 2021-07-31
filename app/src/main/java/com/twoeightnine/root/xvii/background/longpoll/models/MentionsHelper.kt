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

package com.twoeightnine.root.xvii.background.longpoll.models

import com.twoeightnine.root.xvii.storage.SessionProvider

object MentionsHelper {

    private const val MENTION_ALL = "@all"
    private const val MENTION_ONLINE = "@online"

    // userId to substring
    private val cache: MutableMap<Int, String> by lazy { mutableMapOf() }

    private val mentionSubstring: String
        get() = cache.getOrPut(SessionProvider.userId, ::createMentionSubstring)

    fun getMentionTypeIfAny(messageText: String): MentionType? {
        return when {
            mentionSubstring in messageText -> MentionType.YOU
            MENTION_ALL in messageText -> MentionType.ALL
            MENTION_ONLINE in messageText -> MentionType.ONLINE
            else -> null
        }
    }

    private fun createMentionSubstring(): String = "[id${SessionProvider.userId}|"

    enum class MentionType {
        YOU,
        ALL,
        ONLINE
    }
}