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

import android.text.Html
import com.google.gson.internal.LinkedTreeMap
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.asChatPeerId

object LongPollEventFactory {

    fun create(update: ArrayList<Any>): BaseLongPollEvent? {
        val type = (update[0] as Double).toInt()
        return try {
            when (type) {
                BaseLongPollEvent.TYPE_INSTALL_FLAGS -> InstallFlagsEvent(
                        update.asInt(1),
                        update.asInt(2),
                        update.asInt(3)
                )
                BaseLongPollEvent.TYPE_NEW_MESSAGE -> NewMessageEvent(
                        update.asInt(1),
                        update.asInt(2),
                        update.asInt(3),
                        update.asInt(4),
                        Html.fromHtml(update.asString(5)).toString(),
                        BaseMessageEvent.MessageInfo.fromLinkedTreeMap(update[6] as LinkedTreeMap<String, Any>),
                        update.asInt(7)
                )
                BaseLongPollEvent.TYPE_EDIT_MESSAGE -> EditMessageEvent(
                        update.asInt(1),
                        update.asInt(2),
                        update.asInt(3),
                        update.asInt(4),
                        Html.fromHtml(update.asString(5)).toString(),
                        BaseMessageEvent.MessageInfo.fromLinkedTreeMap(update[6] as LinkedTreeMap<String, Any>)
                )
                BaseLongPollEvent.TYPE_READ_INCOMING -> ReadIncomingEvent(update.asInt(1), update.asInt(2))
                BaseLongPollEvent.TYPE_READ_OUTGOING -> ReadOutgoingEvent(update.asInt(1), update.asInt(2))
                BaseLongPollEvent.TYPE_ONLINE -> OnlineEvent(-update.asInt(1), update.asInt(2), update.asInt(3))
                BaseLongPollEvent.TYPE_OFFLINE -> OfflineEvent(-update.asInt(1), update.asInt(3))
                BaseLongPollEvent.TYPE_DELETE_MESSAGES -> DeleteMessagesEvent(update.asInt(1))
                BaseLongPollEvent.TYPE_TYPING -> TypingEvent(update.asInt(1))
                BaseLongPollEvent.TYPE_TYPING_CHAT -> TypingChatEvent(update.asInt(2).asChatPeerId())
                BaseLongPollEvent.TYPE_RECORDING_AUDIO -> RecordingAudioEvent(update.asInt(1))
                BaseLongPollEvent.TYPE_COUNT -> UnreadCountEvent(update.asInt(1))
                else -> null
            }
        } catch (e: Exception) {
            L.def().warn()
                    .throwable(e)
                    .log("unable to create longpoll event")
            null
        }
    }

    fun createAll(updates: ArrayList<ArrayList<Any>>): ArrayList<BaseLongPollEvent> {
        val events = arrayListOf<BaseLongPollEvent>()
        for (update in updates) {
            events.add(create(update) ?: continue)
        }
        return events
    }

    private fun ArrayList<Any>.asInt(position: Int) = (this[position] as Double).toInt()
    private fun ArrayList<Any>.asString(position: Int) = this[position] as String
}