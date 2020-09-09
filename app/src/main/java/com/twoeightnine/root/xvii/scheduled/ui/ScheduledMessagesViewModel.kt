package com.twoeightnine.root.xvii.scheduled.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.scheduled.core.ScheduledMessage
import com.twoeightnine.root.xvii.scheduled.core.SendMessageWorker
import com.twoeightnine.root.xvii.utils.EventBus
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ScheduledMessagesViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val scheduledMessagesLiveData = MutableLiveData<List<ScheduledMessage>>()
    private val peersMapLiveData = MutableLiveData<Map<Int, String>>()

    private var closestWhenMs = 0L

    val scheduledMessages: LiveData<List<ScheduledMessage>>
        get() = scheduledMessagesLiveData

    val peersMap: LiveData<Map<Int, String>>
        get() = peersMapLiveData

    @Inject
    lateinit var appDb: AppDb

    init {
        App.appComponent?.inject(this)
        EventBus.subscribeLongPollEventReceived { event ->
            if (event is NewMessageEvent && closestWhenMs <= System.currentTimeMillis()) {
                loadScheduledMessages()
            }
        }
    }

    fun loadScheduledMessages() {
        appDb.scheduledMessagesDao()
                .getActualScheduledMessages()
                .compose(applySingleSchedulers())
                .subscribe({ scheduledMessages ->
                    val peerIds = scheduledMessages.map { it.peerId }.distinct()
                    loadPeers(peerIds) { peersMap ->
                        peersMapLiveData.value = peersMap
                        scheduledMessagesLiveData.value = scheduledMessages
                        if (scheduledMessages.isNotEmpty()) {
                            closestWhenMs = scheduledMessages[0].whenMs
                        }
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                    lw("error loading messages: ${throwable.message}")
                })
                .addToDisposables()
    }

    fun cancelScheduledMessage(context: Context, scheduledMessage: ScheduledMessage) {
        SendMessageWorker.cancelWorker(context, scheduledMessage.id)
        appDb.scheduledMessagesDao()
                .deleteScheduledMessage(scheduledMessage)
                .compose(applySingleSchedulers())
                .subscribe({
                    loadScheduledMessages()
                }, { throwable ->
                    throwable.printStackTrace()
                    lw("error deleting a message: ${throwable.message}")
                })
                .addToDisposables()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun loadPeers(peerIds: List<Int>, onSuccess: (Map<Int, String>) -> Unit) {
        appDb.dialogsDao()
                .getDialogsByPeerIds(peerIds)
                .map { dialogs -> dialogs.map { dialog -> dialog.peerId to (dialog.alias ?: dialog.title) } }
                .map { pairList -> pairList.toMap() }
                .compose(applySingleSchedulers())
                .subscribe(onSuccess, { throwable ->
                    throwable.printStackTrace()
                    lw("error fetching peers: ${throwable.message}")
                })
                .addToDisposables()
    }

    private fun Disposable.addToDisposables() {
        compositeDisposable.add(this)
    }

    private fun lw(s: String) {
        Lg.wtf("[scheduled] $s")
    }
}