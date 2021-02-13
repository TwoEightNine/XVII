package global.msnthrp.xvii.core.journal.model

import global.msnthrp.xvii.core.utils.MyersDiff

data class MessageJEWithDiff(
     val message: JournalEvent.MessageJE,
     val difference: List<MyersDiff.Change<String>>? = null
)