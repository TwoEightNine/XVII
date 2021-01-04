package com.twoeightnine.root.xvii.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.base.BaseViewModel
import com.twoeightnine.root.xvii.journal.message.model.MessageEvent
import com.twoeightnine.root.xvii.journal.message.model.MessageInfo
import com.twoeightnine.root.xvii.journal.online.model.OnlineEvent
import com.twoeightnine.root.xvii.journal.online.model.OnlineInfo
import com.twoeightnine.root.xvii.utils.DefaultPeerResolver
import global.msnthrp.xvii.core.journal.JournalUseCase
import global.msnthrp.xvii.core.journal.model.JournalEvent
import global.msnthrp.xvii.core.journal.model.JournalEventWithPeer
import global.msnthrp.xvii.data.journal.MemoryJournalDataSource

class JournalViewModel : BaseViewModel() {

    private val journalUseCase by lazy {
        JournalUseCase(MemoryJournalDataSource, DefaultPeerResolver())
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

    fun loadEvents() {
        onIoThread(journalUseCase::getEvents) { events ->
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

        onIoThread({ journalUseCase.getMessageEvents(messageEvent.messageId) }) { events ->
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