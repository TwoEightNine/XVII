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