package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.longpoll.models.events.*
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.DialogsFragmentView
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

open class DialogsFragmentPresenter(override var api: ApiService) : BasePresenter<DialogsFragmentView>(api) {

    var COUNT_HISTORY = 40
    var COUNT_DELETE = 10000

    var dialogs: MutableList<Message> = mutableListOf()
    var users: HashMap<Int, User> = HashMap()
    private var groups: HashMap<Int, Group> = HashMap()
    private var dialogsBuffer: MutableList<Message> = mutableListOf()
    private var longPollDisposable: Disposable? = null

    private var withClear: Boolean = false

    @Inject
    lateinit var apiUtils: ApiUtils

    init {
        longPollDisposable = EventBus.subscribeLongPollEventReceived(::onUpdate)
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
                .subscribeSmart({ response ->
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
                    if (!isOnline()) {
                        CacheHelper.getUsersAsync(userIds) {
                            it.first.forEach {
                                users.put(it.id, it)
                            }
                            setOffline()
                            if (it.second.isNotEmpty() || it.first.isNotEmpty()) {
                                loadUsers(it.second)
                            } else {
                                view?.onDialogsLoaded(mutableListOf())
                            }
                        }
                    } else {
                        loadUsers(userIds.joinToString(separator = ","))
                    }

                }, { error ->
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
                .subscribeSmart({ response ->
                    CacheHelper.saveUsersAsync(response)
                    response.map { users.put(it.id, it) }
                    insertUsers()
                }, { error ->
                    view?.showError(error)
                })
    }

    fun insertUsers(cache: Boolean = false) {
        val newDialogs = MutableList(
                dialogsBuffer.size,
                { index ->
                    dialogsBuffer
                            .map {
                                if (isFromUser(it))
                                    fillDialogUser(it)
                                else if (isFromChat(it))
                                    fillDialogChat(it)
                                else
                                    it
                            }[index]
                }
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
                  filter: (Message) -> Boolean, isGroup: Boolean = false) = messages
            .filter(filter)
            .map { it.userId * (if (isGroup) -1 else 1) }
            .toMutableList()

    fun loadGroups(groupIds: String, cache: Boolean = false) {
        if (groupIds.isEmpty() || cache) {
            insertGroups(cache)
            return
        }
        api.getGroups(groupIds)
                .subscribeSmart({ response ->
                    CacheHelper.saveGroupsAsync(response)
                    response.map { groups.put(it.id, it) }
                    insertGroups(cache)
                }, { error ->
                    view?.showError(error)
                })
    }

    fun loadNewDialog(event: NewMessageEvent) {
        if (event.peerId > 2000000000) {
            val message = getMessageFromLongPoll(event)
            view?.onMessageNew(message)
        } else if (event.peerId > 1000000000) {
            loadGroup(event)
        } else {
            loadUser(event)
        }
    }

    fun loadGroup(event: NewMessageEvent) {
        api.getGroups("${event.peerId - 1000000000}")
                .subscribeSmart({ response ->
                    response.forEach {
                        groups.put(it.id, it)
                    }
                    CacheHelper.saveGroupsAsync(response)
                    var message = getMessageFromLongPoll(event)
                    message.userId = 1000000000 - event.peerId
                    message = fillDialogGroup(message)
                    view?.onMessageNew(message)
                }, {
                    view?.showError(it)
                })
    }

    fun loadUser(event: NewMessageEvent) {
        api.getUsers("${event.peerId}", User.FIELDS)
                .subscribeSmart({ response ->
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
        val flowable: Flowable<BaseResponse<Int>>
        if (dialog.chatId == 0) {
            flowable = api.deleteDialogUser(dialog.userId, COUNT_DELETE)
        } else {
            flowable = api.deleteDialogChat(dialog.chatId, COUNT_DELETE)
        }
        flowable
                .subscribeSmart({ _ ->
                    view?.onRemoveDialog(position)
                }, { error ->
                    view?.showError(error)
                })
    }

    fun readDialog(dialog: Message) {
        api.markAsRead("${dialog.id}")
                .compose(applySchedulers())
                .subscribe()
    }

    fun onUpdate(event: BaseLongPollEvent) {
        when (event) {
            is ReadOutgoingEvent -> view?.onMessageReadOut(event.peerId, event.mid)
            is ReadIncomingEvent -> view?.onMessageReadIn(event.peerId, event.mid)
            is OnlineEvent -> view?.onOnlineChanged(event.userId, true)
            is OfflineEvent -> view?.onOnlineChanged(event.userId, false)
            is NewMessageEvent -> view?.onMessageReceived(event)
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
        dialog.title = user?.fullName ?: "ERR"
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