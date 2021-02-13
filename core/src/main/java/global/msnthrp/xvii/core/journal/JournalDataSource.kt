package global.msnthrp.xvii.core.journal

import global.msnthrp.xvii.core.journal.model.JournalEvent

interface JournalDataSource {

    fun addJournalEvent(journalEvent: JournalEvent)

    fun getJournalEvents(): List<JournalEvent>

    fun clearAll()

    /**
     * events with timeStamp < [recentThresholdTimeStamp] should be deleted
     * @param recentThresholdTimeStamp timeStamp of obsolescence threshold
     */
    fun clearAllExceptRecent(recentThresholdTimeStamp: Long)
}