package com.twoeightnine.root.xvii.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.base.BaseViewModel
import com.twoeightnine.root.xvii.utils.DefaultPeerResolver
import global.msnthrp.xvii.core.journal.JournalUseCase
import global.msnthrp.xvii.core.journal.model.JournalEventWithPeer
import global.msnthrp.xvii.data.journal.MemoryJournalDataSource

class JournalViewModel : BaseViewModel() {

    private val journalUseCase by lazy {
        JournalUseCase(MemoryJournalDataSource, DefaultPeerResolver())
    }

    private val eventsLiveData = MutableLiveData<List<JournalEventWithPeer>>()

    val events: LiveData<List<JournalEventWithPeer>>
        get() = eventsLiveData

    fun loadEvents() {
        onIoThread(journalUseCase::getEvents) { events ->
            eventsLiveData.value = events
        }
    }

}