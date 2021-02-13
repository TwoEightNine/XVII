package global.msnthrp.xvii.core.journal

import global.msnthrp.xvii.core.journal.model.JournalEvent
import global.msnthrp.xvii.core.journal.model.JournalEventWithPeer
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

    fun getEvents(): List<JournalEventWithPeer> = getAllJournalEvents().toEventsWithPeer()

    fun getOnlineEvents(userId: Int): List<JournalEvent.StatusJE> =
            getAllJournalEvents()
                    .filterIsInstance<JournalEvent.StatusJE>()
                    .filter { it.peerId == userId }

    fun getMessageEvents(messageId: Int): List<JournalEvent.MessageJE> =
            getAllJournalEvents()
                    .filterIsInstance<JournalEvent.MessageJE>()
                    .filter { it.messageId == messageId }

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