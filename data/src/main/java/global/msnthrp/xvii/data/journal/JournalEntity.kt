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

package global.msnthrp.xvii.data.journal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import global.msnthrp.xvii.core.journal.model.JournalEvent

@Entity(tableName = "journal")
data class JournalEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,

        val type: Int = 0,
        @ColumnInfo(name = "peer_id")
        val peerId: Int = 0,
        @ColumnInfo(name = "time_stamp")
        val timeStamp: Long = 0L,

        @ColumnInfo(name = "message_id")
        val messageId: Int? = null,
        @ColumnInfo(name = "from_id")
        val fromId: Int? = null,

        @ColumnInfo(name = "device_code")
        val deviceCode: Int? = null,
        @ColumnInfo(name = "last_seen")
        val lastSeen: Long? = null,

        val text: String? = null
) {

    fun toJournalEvent(): JournalEvent? {
        return when (type) {
            TYPE_DELETED_MESSAGE -> messageId?.let {
                JournalEvent.MessageJE.DeletedMessageJE(peerId, timeStamp, messageId)
            }
            TYPE_NEW_MESSAGE -> messageId?.let {
                fromId?.let {
                    text?.let {
                        JournalEvent.MessageJE.NewMessageJE(peerId, timeStamp, messageId, fromId, text)
                    }
                }
            }
            TYPE_EDITED_MESSAGE -> messageId?.let {
                fromId?.let {
                    text?.let {
                        JournalEvent.MessageJE.EditedMessageJE(peerId, timeStamp, messageId, fromId, text)
                    }
                }
            }
            TYPE_READ_MESSAGE -> messageId?.let {
                JournalEvent.MessageJE.ReadMessageJE(peerId, timeStamp, messageId)
            }

            TYPE_TYPING_ACTIVITY -> fromId?.let {
                JournalEvent.ActivityJE.TypingActivityJE(peerId, timeStamp, fromId)
            }
            TYPE_RECORDING_ACTIVITY -> fromId?.let {
                JournalEvent.ActivityJE.RecordingActivityJE(peerId, timeStamp, fromId)
            }

            TYPE_ONLINE_STATUS -> deviceCode?.let {
                JournalEvent.StatusJE.OnlineStatusJE(peerId, timeStamp, deviceCode)
            }
            TYPE_OFFLINE_STATUS -> lastSeen?.let {
                JournalEvent.StatusJE.OfflineStatusJE(peerId, timeStamp, lastSeen)
            }
            else -> null
        }
    }


    companion object {

        private const val TYPE_DELETED_MESSAGE = 1
        private const val TYPE_NEW_MESSAGE = 2
        private const val TYPE_EDITED_MESSAGE = 3
        private const val TYPE_READ_MESSAGE = 4

        private const val TYPE_TYPING_ACTIVITY = 5
        private const val TYPE_RECORDING_ACTIVITY = 6

        private const val TYPE_ONLINE_STATUS = 7
        private const val TYPE_OFFLINE_STATUS = 8

        fun from(journalEvent: JournalEvent): JournalEntity {
            val type = getType(journalEvent)
            val peerId = journalEvent.peerId
            val timeStamp = journalEvent.timeStamp

            val messageId = (journalEvent as? JournalEvent.MessageJE)?.messageId
            val fromId = (journalEvent as? JournalEvent.MessageJE.NewMessageJE)?.fromId
                    ?: (journalEvent as? JournalEvent.MessageJE.EditedMessageJE)?.fromId
                    ?: (journalEvent as? JournalEvent.ActivityJE)?.fromId

            val deviceCode = (journalEvent as? JournalEvent.StatusJE.OnlineStatusJE)?.deviceCode
            val lastSeen = (journalEvent as? JournalEvent.StatusJE.OfflineStatusJE)?.lastSeen

            val text = (journalEvent as? JournalEvent.MessageJE.NewMessageJE)?.messageText
                    ?: (journalEvent as? JournalEvent.MessageJE.EditedMessageJE)?.messageText

            return JournalEntity(
                    type = type,
                    peerId = peerId,
                    timeStamp = timeStamp,

                    messageId = messageId,
                    fromId = fromId,

                    deviceCode = deviceCode,
                    lastSeen = lastSeen,

                    text = text
            )

        }

        fun getType(journalEvent: JournalEvent): Int = when(journalEvent) {
            is JournalEvent.MessageJE.DeletedMessageJE -> TYPE_DELETED_MESSAGE
            is JournalEvent.MessageJE.NewMessageJE -> TYPE_NEW_MESSAGE
            is JournalEvent.MessageJE.EditedMessageJE -> TYPE_EDITED_MESSAGE
            is JournalEvent.MessageJE.ReadMessageJE -> TYPE_READ_MESSAGE

            is JournalEvent.ActivityJE.TypingActivityJE -> TYPE_TYPING_ACTIVITY
            is JournalEvent.ActivityJE.RecordingActivityJE -> TYPE_RECORDING_ACTIVITY

            is JournalEvent.StatusJE.OnlineStatusJE -> TYPE_ONLINE_STATUS
            is JournalEvent.StatusJE.OfflineStatusJE -> TYPE_OFFLINE_STATUS
        }
    }
}