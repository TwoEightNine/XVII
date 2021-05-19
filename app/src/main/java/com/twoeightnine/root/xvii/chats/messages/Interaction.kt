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

package com.twoeightnine.root.xvii.chats.messages

import com.twoeightnine.root.xvii.model.messages.WrappedMessage

/**
 * for interaction with messages
 */
data class Interaction(
        val type: Type,

        /**
         * from which to apply changes
         */
        val position: Int = 0,

        /**
         * changed messages in natural ui order (eldest first)
         */
        val messages: List<WrappedMessage> = arrayListOf()
) {

    enum class Type {
        ADD,
        UPDATE,
        REMOVE,
        CLEAR
    }
}