package global.msnthrp.xvii.data.journal

import global.msnthrp.xvii.core.journal.JournalDataSource
import global.msnthrp.xvii.core.journal.model.JournalEvent

class DbJournalDataSource(private val journalDao: JournalDao) : JournalDataSource {

    override fun addJournalEvent(journalEvent: JournalEvent) {
        journalDao.insertEvent(journalEvent.let(JournalEntity::from))
    }

    override fun getJournalEvents(): List<JournalEvent> {
        return journalDao.getAllEvents().mapNotNull(JournalEntity::toJournalEvent)
    }

    override fun clearAll() {
        clearAllExceptRecent(0L)
    }

    override fun clearAllExceptRecent(recentThresholdTimeStamp: Long) {
        journalDao.clearAllExceptRecent(recentThresholdTimeStamp)
    }
}