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

package com.twoeightnine.root.xvii.chats.tools

import android.content.Context

class ChatStorage(context: Context) {

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    /**
     * message of user that was not sent
     */
    fun getMessageText(peerId: Int): String? = prefs.getString(nameFor(peerId), null)

    fun setMessageText(peerId: Int, value: String) = prefs.edit().putString(nameFor(peerId), value).apply()

    private fun nameFor(peerId: Int) = "$MESSAGE_TEXT$peerId"

    companion object {

        private const val NAME = "chatStorage"
        private const val MESSAGE_TEXT = "messageText"
    }
}