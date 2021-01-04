package com.twoeightnine.root.xvii.journal.online.model

import android.os.Parcelable
import global.msnthrp.xvii.core.journal.model.JournalEvent
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OnlineEvent(
        val time: Int,
        val isOnline: Boolean,
        val deviceCode: Int
) : Parcelable {

    companion object {

        fun fromJournalEvent(event: JournalEvent.StatusJE) = OnlineEvent(
                time = (event.timeStamp / 1000L).toInt(),
                isOnline = event is JournalEvent.StatusJE.OnlineStatusJE,
                deviceCode = (event as? JournalEvent.StatusJE.OnlineStatusJE)?.deviceCode ?: 0
        )
    }
}