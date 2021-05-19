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

package com.twoeightnine.root.xvii.background.longpoll.models.events

abstract class BaseLongPollEvent {

    abstract fun getType(): Int

    companion object {
        const val TYPE_INSTALL_FLAGS = 2
        const val TYPE_NEW_MESSAGE = 4
        const val TYPE_EDIT_MESSAGE = 5
        const val TYPE_READ_INCOMING = 6
        const val TYPE_READ_OUTGOING = 7
        const val TYPE_ONLINE = 8
        const val TYPE_OFFLINE = 9
        const val TYPE_DELETE_MESSAGES = 13
        const val TYPE_TYPING = 61
        const val TYPE_TYPING_CHAT = 62
        const val TYPE_RECORDING_AUDIO = 64
        const val TYPE_COUNT = 80

    }
}