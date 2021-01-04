package com.twoeightnine.root.xvii.journal.message.model

import android.os.Parcelable
import global.msnthrp.xvii.core.journal.model.JournalEvent
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageEvent(
        val messageId: Int,
        val time: Int,
        val messageText: String,
        val isDeleted: Boolean = false
) : Parcelable{

    companion object {

        fun fromJournalEvent(event: JournalEvent.MessageJE) = MessageEvent(
                messageId = event.messageId,
                time = (event.timeStamp / 1000L).toInt(),
                isDeleted = event is JournalEvent.MessageJE.DeletedMessageJE,
                messageText = (event as? JournalEvent.MessageJE.NewMessageJE)?.messageText
                        ?: (event as? JournalEvent.MessageJE.EditedMessageJE)?.messageText
                        ?: ""
        )
    }
}