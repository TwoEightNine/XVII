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

package global.msnthrp.xvii.core.journal.model

sealed class JournalEvent(
        val peerId: Int,

        val timeStamp: Long
) {

    sealed class MessageJE(
            peerId: Int,
            timeStamp: Long,

            val messageId: Int
    ) : JournalEvent(peerId, timeStamp) {

        class DeletedMessageJE(
                peerId: Int,
                timeStamp: Long,
                messageId: Int
        ) : MessageJE(peerId, timeStamp, messageId)

        class NewMessageJE(
                peerId: Int,
                timeStamp: Long,
                messageId: Int,

                val fromId: Int = peerId,
                val messageText: String
        ) : MessageJE(peerId, timeStamp, messageId)

        class EditedMessageJE(
                peerId: Int,
                timeStamp: Long,
                messageId: Int,

                val fromId: Int = peerId,
                val messageText: String
        ) : MessageJE(peerId, timeStamp, messageId)

        class ReadMessageJE(
                peerId: Int,
                timeStamp: Long,
                messageId: Int
        ) : MessageJE(peerId, timeStamp, messageId)

    }

    sealed class ActivityJE(
            peerId: Int,
            timeStamp: Long,

            val fromId: Int = peerId
    ) : JournalEvent(peerId, timeStamp) {

        class TypingActivityJE(peerId: Int, timeStamp: Long, fromId: Int = peerId) : ActivityJE(peerId, timeStamp, fromId)

        class RecordingActivityJE(peerId: Int, timeStamp: Long, fromId: Int = peerId) : ActivityJE(peerId, timeStamp, fromId)
    }

    sealed class StatusJE(
            peerId: Int,
            timeStamp: Long
    ) : JournalEvent(peerId, timeStamp) {

        class OnlineStatusJE(
                peerId: Int,
                timeStamp: Long,

                val deviceCode: Int
        ) : StatusJE(peerId, timeStamp)

        class OfflineStatusJE(
                peerId: Int,
                timeStamp: Long,

                val lastSeen: Long
        ) : StatusJE(peerId, timeStamp)
    }
}
