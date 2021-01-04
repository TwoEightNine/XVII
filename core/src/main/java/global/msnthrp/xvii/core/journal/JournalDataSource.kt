package global.msnthrp.xvii.core.journal

import global.msnthrp.xvii.core.journal.model.JournalEvent

interface JournalDataSource {

    fun addJournalEvent(journalEvent: JournalEvent)

    fun getJournalEvents(): List<JournalEvent>

    fun clearAll()

    fun clearAllExceptRecent(recentThreshold: Long)
}