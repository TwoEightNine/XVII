package global.msnthrp.xvii.core.journal.model

data class JournalEventWithPeer(
        val journalEvent: JournalEvent,

        val peerName: String,
        val peerPhoto: String,

        val fromName: String = peerName,
        val fromPhoto: String = peerPhoto
)