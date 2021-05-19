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

package global.msnthrp.xvii.core.journal

import global.msnthrp.xvii.core.journal.model.JournalEvent
import global.msnthrp.xvii.core.journal.model.JournalEventWithPeer
import global.msnthrp.xvii.core.journal.model.JournalFilter
import global.msnthrp.xvii.core.journal.model.MessageJEWithDiff
import global.msnthrp.xvii.core.utils.MyersDiff
import global.msnthrp.xvii.core.utils.PeerResolver
import java.util.concurrent.TimeUnit

class JournalUseCase(
        private val journalDataSource: JournalDataSource,
        private val peerResolver: PeerResolver? = null
) {

    fun addUserOnline(userId: Int, deviceCode: Int, timeStamp: Long = System.currentTimeMillis()) {
        journalDataSource.addJournalEvent(JournalEvent.StatusJE.OnlineStatusJE(userId, timeStamp, deviceCode))
    }

    fun addUserOffline(userId: Int, lastSeen: Long, timeStamp: Long = System.currentTimeMillis()) {
        journalDataSource.addJournalEvent(JournalEvent.StatusJE.OfflineStatusJE(userId, timeStamp, lastSeen))
    }

    fun addActivity(peerId: Int, isVoice: Boolean, fromId: Int = peerId, timeStamp: Long = System.currentTimeMillis()) {
        val event = if (isVoice) {
            JournalEvent.ActivityJE.RecordingActivityJE(peerId, timeStamp, fromId)
        } else {
            JournalEvent.ActivityJE.TypingActivityJE(peerId, timeStamp, fromId)
        }
        journalDataSource.addJournalEvent(event)
    }

    fun addMessageDeleted(peerId: Int, messageId: Int, timeStamp: Long = System.currentTimeMillis()) {
        journalDataSource.addJournalEvent(JournalEvent.MessageJE.DeletedMessageJE(peerId, timeStamp, messageId))
    }

    fun addMessage(peerId: Int, messageId: Int, messageText: String, isEdited: Boolean, fromId: Int = peerId, timeStamp: Long = System.currentTimeMillis()) {
        val event = if (isEdited) {
            JournalEvent.MessageJE.EditedMessageJE(peerId, timeStamp, messageId, fromId, messageText)
        } else {
            JournalEvent.MessageJE.NewMessageJE(peerId, timeStamp, messageId, fromId, messageText)
        }
        journalDataSource.addJournalEvent(event)
    }

    fun addReadMessage(peerId: Int, messageId: Int, timeStamp: Long = System.currentTimeMillis()) {
        journalDataSource.addJournalEvent(JournalEvent.MessageJE.ReadMessageJE(peerId, timeStamp, messageId))
    }

    fun getEvents(filter: JournalFilter = JournalFilter.ALL): List<JournalEventWithPeer> {
        val events = getAllJournalEvents()
        val filteredEvents = when (filter) {
            JournalFilter.ALL -> events
            JournalFilter.EDITED_MESSAGES -> events.filterIsInstance<JournalEvent.MessageJE.EditedMessageJE>()
            JournalFilter.DELETED_MESSAGES -> events.filterIsInstance<JournalEvent.MessageJE.DeletedMessageJE>()
            JournalFilter.STATUSES -> events.filterIsInstance<JournalEvent.StatusJE>()
        }

        return filteredEvents.toEventsWithPeer()
    }

    fun getOnlineEvents(userId: Int): List<JournalEvent.StatusJE> =
            getAllJournalEvents()
                    .filterIsInstance<JournalEvent.StatusJE>()
                    .filter { it.peerId == userId }

    fun getMessageEvents(messageId: Int): List<JournalEvent.MessageJE> =
            getAllJournalEvents()
                    .filterIsInstance<JournalEvent.MessageJE>()
                    .filter { it.messageId == messageId }

    fun getMessageEventsWithDiffs(
            messageId: Int
    ): List<MessageJEWithDiff> {
        val messageEvents = getMessageEvents(messageId)
        val result = arrayListOf<MessageJEWithDiff>()
        for (messageEvent in messageEvents) {
            if (messageEvent !is JournalEvent.MessageJE.EditedMessageJE) {
                result.add(MessageJEWithDiff(messageEvent))
                continue
            }

            val previousEvent = messageEvents.getOrNull(messageEvents.indexOf(messageEvent) - 1)
            if (previousEvent == null) {
                result.add(MessageJEWithDiff(messageEvent))
                continue
            }

            val textBefore = when (previousEvent) {
                is JournalEvent.MessageJE.EditedMessageJE -> previousEvent.messageText
                is JournalEvent.MessageJE.NewMessageJE -> previousEvent.messageText
                else -> null
            }
            if (textBefore == null) {
                result.add(MessageJEWithDiff(messageEvent))
                continue
            }

            val difference = MyersDiff.getDiffByWordsAndSigns(textBefore, messageEvent.messageText)
            result.add(MessageJEWithDiff(messageEvent, difference))
        }
        return result
    }

    private fun getAllJournalEvents(): List<JournalEvent> {
        journalDataSource.clearAllExceptRecent(System.currentTimeMillis() - RECENT_THRESHOLD)
        return journalDataSource.getJournalEvents()
    }

    private fun List<JournalEvent>.toEventsWithPeer(): List<JournalEventWithPeer> {
        val peers = peerResolver?.resolvePeers(map { it.peerId }) ?: mapOf()
        return map { event ->
            JournalEventWithPeer(
                    journalEvent = event,
                    peerName = peers[event.peerId]?.peerName ?: "id${event.peerId}",
                    peerPhoto = peers[event.peerId]?.peerPhoto ?: ""
            )
        }
    }

    companion object {
        private val RECENT_THRESHOLD = TimeUnit.DAYS.toMillis(2L)
    }

}