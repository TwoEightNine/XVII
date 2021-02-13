package com.twoeightnine.root.xvii.journal.message.model

import android.os.Parcelable
import global.msnthrp.xvii.core.journal.model.JournalEvent
import global.msnthrp.xvii.core.journal.model.MessageJEWithDiff
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageEvent(
        val messageId: Int,
        val time: Int,
        val messageText: String,
        val isDeleted: Boolean = false,
        val difference: List<Change>? = null
) : Parcelable {

    companion object {

        fun fromJournalEvent(event: MessageJEWithDiff) = MessageEvent(
                messageId = event.message.messageId,
                time = (event.message.timeStamp / 1000L).toInt(),
                isDeleted = event.message is JournalEvent.MessageJE.DeletedMessageJE,
                messageText = (event.message as? JournalEvent.MessageJE.NewMessageJE)?.messageText
                        ?: (event.message as? JournalEvent.MessageJE.EditedMessageJE)?.messageText
                        ?: "",
                difference = event.difference?.let(MessageDifference::from)
        )
    }
}