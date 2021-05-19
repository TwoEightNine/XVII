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