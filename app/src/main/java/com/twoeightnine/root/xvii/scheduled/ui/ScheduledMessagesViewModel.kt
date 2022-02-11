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

package com.twoeightnine.root.xvii.scheduled.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.scheduled.core.SendMessageWorker
import com.twoeightnine.root.xvii.utils.EventBus
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import global.msnthrp.xvii.data.db.AppDb
import global.msnthrp.xvii.data.scheduled.ScheduledMessage
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
                    lw("error loading messages", throwable)
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
                    lw("error deleting a message", throwable)
                })
                .addToDisposables()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun loadPeers(peerIds: List<Int>, onSuccess: (Map<Int, String>) -> Unit) {
        appDb.dialogsDao()
                .getLargeListOfDialogs(peerIds)
                .map { dialogs -> dialogs.map { dialog -> dialog.peerId to dialog.aliasOrTitle } }
                .map { pairList -> pairList.toMap() }
                .compose(applySingleSchedulers())
                .subscribe(onSuccess, { throwable ->
                    lw("error fetching peers", throwable)
                })
                .addToDisposables()
    }

    private fun Disposable.addToDisposables() {
        compositeDisposable.add(this)
    }

    private fun lw(s: String, throwable: Throwable? = null) {
        L.tag("scheduled messages")
                .throwable(throwable)
                .log(s)
    }
}