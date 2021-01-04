package global.msnthrp.xvii.data.journal

import global.msnthrp.xvii.core.journal.JournalDataSource
import global.msnthrp.xvii.core.journal.model.JournalEvent

object MemoryJournalDataSource : JournalDataSource {

    private val events = arrayListOf<JournalEvent>()

    override fun addJournalEvent(journalEvent: JournalEvent) {
        events.add(journalEvent)
    }

    override fun getJournalEvents(): List<JournalEvent> {
        return events
    }

    override fun clearAll() {
        events.clear()
    }

    override fun clearAllExceptRecent(recentThreshold: Long) {
        val recent = events.filter { it.timeStamp >= recentThreshold }
        events.clear()
        events.addAll(recent)
    }
}