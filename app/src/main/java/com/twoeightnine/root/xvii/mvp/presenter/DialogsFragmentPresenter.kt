package com.twoeightnine.root.xvii.mvp.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.notifications.NotificationsCore
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.model.response.LongPollResponse
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.DialogsFragmentView
import com.twoeightnine.root.xvii.response.ServerResponse
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.Flowable
import javax.inject.Inject

open class DialogsFragmentPresenter(override var api: ApiService) : BasePresenter<DialogsFragmentView>(api) {

    var COUNT_HISTORY = 40
    var COUNT_DELETE = 10000

    var dialogs: MutableList<Message> = mutableListOf()
    var users: HashMap<Int, User> = HashMap()
    var groups: HashMap<Int, Group> = HashMap()
    var dialogsBuffer: MutableList<Message> = mutableListOf()

    var withClear: Boolean = false

    @Inject
    lateinit var apiUtils: ApiUtils

    var receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            onUpdate((intent.extras.getSerializable(NotificationsCore.RESULT) as LongPollResponse).updates ?: mutableListOf())
        }
    }

    init {
        LocalBroadcastManager.getInstance(App.context).registerReceiver(receiver, IntentFilter(NotificationsCore.NAME))
        App.appComponent?.inject(this)
    }

    open fun getSaved() = dialogs

    fun loadDialogs(offset: Int = 0, withClear: Boolean = false) {
        view?.showLoading()
        if (offset == 0) {
            dialogs.clear()
        }
        this.withClear = withClear
        api.getDialogs(offset, COUNT_HISTORY)
                .subscribeSmart({
                    response ->
                    dialogsBuffer.clear()
                    response.items.map { dialogsBuffer.add(setCounters(it)) }
                    val userIds = getAllIds(dialogsBuffer, { isFromUser(it) })
                    dialogsBuffer.forEach {
                        if (it.chatId != 0) {
                            val chat = UserDb(it)
                            users.put(chat.id, User(chat))
                        }
                    }
                    CacheHelper.saveUsersAsync(users.entries.map { it.value }.toMutableList())
                    CacheHelper.getUsersAsync(userIds, {
                        it.first.forEach {
                            users.put(it.id, it)
                        }
                        setOffline()
                        if (it.second.isNotEmpty() || it.first.isNotEmpty()) {
                            loadUsers(it.second)
                        } else {
                            view?.onDialogsLoaded(mutableListOf())
                        }
                    })

                }, {
                    error ->
                    view?.showError(error)
                })
    }

    fun loadCachedDialogs() {
        view?.showLoading()
        CacheHelper.getDialogsAsync {
            dialogsBuffer = it
            val userIds = getAllIds(dialogsBuffer, { isFromUser(it) })
            CacheHelper.getUsersAsync(userIds, {
                it.first.forEach {
                    users.put(it.id, it)
                }
                setOffline()
                if (it.second.isNotEmpty() || it.first.isNotEmpty()) {
                    loadUsers(it.second, true)
                } else {
                    view?.onDialogsLoaded(mutableListOf())
                    view?.onCacheRestored()
                }
            })

        }
    }

    fun loadUsers(userIds: String, cache: Boolean = false) {
        if (userIds.isEmpty() || cache) {
            insertUsers(cache)
            return
        }
        api.getUsers(userIds, User.FIELDS)
                .subscribeSmart({
                    response ->
                    CacheHelper.saveUsersAsync(response)
                    response.map { users.put(it.id, it) }
                    insertUsers()
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    fun insertUsers(cache: Boolean = false) {
        val newDialogs = MutableList(
                dialogsBuffer.size,
                {
                    index -> dialogsBuffer
                        .map {
                            if (isFromUser(it))
                                fillDialogUser(it)
                            else if (isFromChat(it))
                                fillDialogChat(it)
                            else
                                it
                        }[index] }
        )
        val groupIds = getAllIds(dialogsBuffer, { isFromGroup(it) }, true)
        CacheHelper.getGroupsAsync(groupIds, {
            it.first.forEach {
                groups.put(it.id, it)
            }
            if (it.second.isEmpty() && it.first.isEmpty()) {
                dialogs.addAll(newDialogs)
                CacheHelper.saveMessagesAsync(newDialogs)
                if (withClear) {
                    dialogs.clear()
                    view?.onDialogsClear()
                    withClear = false
                }
                view?.onDialogsLoaded(newDialogs)
                if (cache) {
                    view?.onCacheRestored()
                }
            } else {
                loadGroups(it.second, cache)
            }
        })

    }

    fun getAllIds(messages: MutableList<Message>,
                  filter: (Message) -> Boolean, isGroup: Boolean = false)
            = messages
            .filter(filter)
            .map { it.userId * (if (isGroup) -1 else 1) }
            .toMutableList()

    fun loadGroups(groupIds: String, cache: Boolean = false) {
        if (groupIds.isEmpty() || cache) {
            insertGroups(cache)
            return
        }
        api.getGroups(groupIds)
                .subscribeSmart({
                    response ->
                    CacheHelper.saveGroupsAsync(response)
                    response.map { groups.put(it.id, it) }
                    insertGroups(cache)
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    fun loadNewDialog(event: LongPollEvent) {
        if (event.userId > 2000000000) {
            val message = getMessageFromLongPoll(event)
            view?.onMessageNew(message)
        } else if (event.userId > 1000000000) {
            loadGroup(event)
        } else {
            loadUser(event)
        }
    }

    fun loadGroup(event: LongPollEvent) {
        api.getGroups("${event.userId - 1000000000}")
                .subscribeSmart({
                    response ->
                    response.forEach {
                        groups.put(it.id, it)
                    }
                    CacheHelper.saveGroupsAsync(response)
                    var message = getMessageFromLongPoll(event)
                    message.userId = 1000000000 - event.userId
                    message = fillDialogGroup(message)
                    view?.onMessageNew(message)
                }, {
                    view?.showError(it)
                })
    }

    fun loadUser(event: LongPollEvent) {
        api.getUsers("${event.userId}", User.FIELDS)
                .subscribeSmart({
                    response ->
                    response.forEach { users.put(it.id, it) }
                    CacheHelper.saveUsersAsync(response)
                    var message = getMessageFromLongPoll(event)
                    message = fillDialogUser(message)
                    view?.onMessageNew(message)
                }, {
                    view?.showError(it)
                })
    }

    fun insertGroups(cache: Boolean = false) {
        val newDialogs = MutableList(
                dialogsBuffer.size,
                { index -> dialogsBuffer.map { if (isFromGroup(it)) fillDialogGroup(it) else it }[index] }
        )
        dialogs.addAll(newDialogs)
        CacheHelper.saveMessagesAsync(newDialogs)
        if (withClear) {
            view?.onDialogsClear()
            withClear = false
        }
        view?.onDialogsLoaded(newDialogs)
        if (cache) {
            view?.onCacheRestored()
        }
    }

    fun deleteDialog(dialog: Message, position: Int) {
        val flowable: Flowable<ServerResponse<Int>>
        if (dialog.chatId == 0) {
            flowable = api.deleteDialogUser(dialog.userId, COUNT_DELETE)
        } else {
            flowable = api.deleteDialogChat(dialog.chatId, COUNT_DELETE)
        }
        flowable
                .subscribeSmart({
                    _ ->
                    view?.onRemoveDialog(position)
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    fun readDialog(dialog: Message) {
        api.markAsRead("${dialog.id}")
                .compose(applySchedulers())
                .subscribe()
    }

    fun onUpdate(data: MutableList<MutableList<Any>>) {
        for (item in data) {
            val event = LongPollEvent(item)
            when (event.type) {
                LongPollEvent.READ_OUT -> view?.onMessageReadOut(event.userId, event.mid)
                LongPollEvent.READ_IN -> view?.onMessageReadIn(event.userId, event.mid)
                LongPollEvent.ONLINE -> view?.onOnlineChanged(event.userId, true)
                LongPollEvent.OFFLINE-> view?.onOnlineChanged(event.userId, false)
                LongPollEvent.NEW_MESSAGE -> view?.onMessageReceived(event)
            }
        }
    }

    fun setOffline() {
        if (Prefs.beOffline) {
            apiUtils.setOffline()
        }
    }

    fun setCounters(container: MessageContainer): Message {
        val dialog = container.message
        dialog?.unread = container.unread
        return dialog!!
    }

    fun fillDialogUser(dialog: Message): Message {
//        if (dialog.chatId != 0) {
//            return dialog
//        }
        val user = users[getPeerId(dialog.userId, dialog.chatId)]
        dialog.title = user?.fullName() ?: "ERR"
        dialog.photo = user?.photo100
        dialog.online = user?.online ?: 0
        dialog.isMute = Prefs.muteList.contains(getPollFrom(dialog.userId, dialog.chatId))
        return dialog
    }

    fun fillDialogChat(dialog: Message): Message {
        val peerId = getPeerId(dialog.userId, dialog.chatId)
        val user = users[peerId]
        dialog.title = user?.firstName ?: "ERR"
        dialog.photo = user?.photo100
        dialog.isMute = Prefs.muteList.contains(getPollFrom(dialog.userId, dialog.chatId))
        return dialog
    }

    fun fillDialogGroup(dialog: Message): Message {
        val group = groups[-dialog.userId]
        dialog.title = group?.name
        dialog.photo = group?.photo100
        return dialog
    }

    fun isFromUser(dialog: Message) = dialog.userId > 0 && dialog.chatId == 0

    fun isFromChat(dialog: Message) = dialog.chatId != 0

    fun isFromGroup(dialog: Message) = (dialog.title?.equals(" ... ") ?: false || dialog.title?.equals("") ?: false) && dialog.userId < 0

}