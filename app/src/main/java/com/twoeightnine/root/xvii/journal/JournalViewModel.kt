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

package com.twoeightnine.root.xvii.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.base.BaseViewModel
import com.twoeightnine.root.xvii.journal.message.model.MessageEvent
import com.twoeightnine.root.xvii.journal.message.model.MessageInfo
import com.twoeightnine.root.xvii.journal.online.model.OnlineEvent
import com.twoeightnine.root.xvii.journal.online.model.OnlineInfo
import com.twoeightnine.root.xvii.utils.DefaultPeerResolver
import global.msnthrp.xvii.core.journal.JournalUseCase
import global.msnthrp.xvii.core.journal.model.JournalEvent
import global.msnthrp.xvii.core.journal.model.JournalEventWithPeer
import global.msnthrp.xvii.core.journal.model.JournalFilter
import global.msnthrp.xvii.data.db.AppDb
import global.msnthrp.xvii.data.journal.DbJournalDataSource
import javax.inject.Inject

class JournalViewModel : BaseViewModel() {

    @Inject
    lateinit var appDb: AppDb

    private val journalDataSource by lazy {
        App.appComponent?.inject(this)
        DbJournalDataSource(appDb.journalDao())
    }

    private val journalUseCase by lazy {
        JournalUseCase(journalDataSource, DefaultPeerResolver())
    }

    private val eventsLiveData = MutableLiveData<List<JournalEventWithPeer>>()
    private val onlineEventsLiveData = MutableLiveData<OnlineInfo>()
    private val messageEventsLiveData = MutableLiveData<MessageInfo>()

    val events: LiveData<List<JournalEventWithPeer>>
        get() = eventsLiveData

    val onlineEvents: LiveData<OnlineInfo>
        get() = onlineEventsLiveData

    val messageEvents: LiveData<MessageInfo>
        get() = messageEventsLiveData

    fun loadEvents(filter: JournalFilter = JournalFilter.ALL) {
        val task = { journalUseCase.getEvents(filter) }
        onIoThread(task) { events ->
            eventsLiveData.value = events
        }
    }

    fun loadOnlineEvents(event: JournalEventWithPeer) {
        val statusEvent = event.journalEvent as? JournalEvent.StatusJE ?: return

        onIoThread({ journalUseCase.getOnlineEvents(statusEvent.peerId) }) { events ->
            onlineEventsLiveData.value = OnlineInfo(
                    userId = statusEvent.peerId,
                    userName = event.peerName,
                    events = events.map(OnlineEvent::fromJournalEvent)
            )
        }
    }

    fun loadMessageEvents(event: JournalEventWithPeer) {
        val messageEvent = event.journalEvent as? JournalEvent.MessageJE ?: return

        onIoThread({ journalUseCase.getMessageEventsWithDiffs(messageEvent.messageId) }) { events ->
            messageEventsLiveData.value = MessageInfo(
                    messageId = messageEvent.messageId,
                    peerId = messageEvent.peerId,
                    peerName = event.peerName,
                    events = events.map(MessageEvent::fromJournalEvent),
                    fromName = event.fromName
            )
        }
    }

}