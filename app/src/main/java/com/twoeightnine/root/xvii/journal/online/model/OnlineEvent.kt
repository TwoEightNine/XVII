package com.twoeightnine.root.xvii.journal.online.model

import android.os.Parcelable
import global.msnthrp.xvii.core.journal.model.JournalEvent
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OnlineEvent(
        val time: Int,
        val isOnline: Boolean,

        /**
         * used only if online
         */
        val deviceCode: Int,

        /**
         * used only if offline
         */
        val lastSeen: Int
) : Parcelable {

    companion object {

        fun fromJournalEvent(event: JournalEvent.StatusJE) = OnlineEvent(
                time = (event.timeStamp / 1000L).toInt(),
                isOnline = event is JournalEvent.StatusJE.OnlineStatusJE,
                deviceCode = (event as? JournalEvent.StatusJE.OnlineStatusJE)?.deviceCode ?: 0,
                lastSeen = (((event as? JournalEvent.StatusJE.OfflineStatusJE)?.lastSeen ?: 0L) / 1000L).toInt()
        )
    }
}