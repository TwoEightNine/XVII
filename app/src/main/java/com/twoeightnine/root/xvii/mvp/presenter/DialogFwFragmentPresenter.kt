package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.MessageContainer
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.DialogFwFragmentView
import com.twoeightnine.root.xvii.utils.getPollFrom
import com.twoeightnine.root.xvii.utils.subscribeSmart

class DialogFwFragmentPresenter(api: ApiService) : BasePresenter<DialogFwFragmentView>(api) {

    private var countHistory = 40

    private var dialogs: MutableList<Message> = mutableListOf()
    private var users: HashMap<Int, User> = HashMap()
    private var groups: HashMap<Int, Group> = HashMap()
    private var dialogsBuffer: MutableList<Message> = mutableListOf()

    fun loadDialogs(offset: Int = 0) {
        view?.showLoading()
        if (offset == 0) {
            dialogs.clear()
        }
        api.getDialogs(offset, countHistory)
                .subscribeSmart({
                    response ->
                    dialogsBuffer.clear()
                    response.items.map { dialogsBuffer.add(setCounters(it)) }
                    val userIds = getAllIds(dialogsBuffer, { isFromUser(it) })
                    if (userIds != "") {
                        loadUsers(userIds)
                    } else {
                        view?.onDialogsLoaded(mutableListOf())
                    }
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    private fun loadUsers(userIds: String) {
        api.getUsers(userIds, User.FIELDS)
                .subscribeSmart({
                    response ->
                    response.map { users.put(it.id, it) }
                    val newDialogs = MutableList(
                            dialogsBuffer.size,
                            { index -> dialogsBuffer.map { if (isFromUser(it)) fillDialogUser(it) else it }[index] }
                    )
                    val groupIds = getAllIds(dialogsBuffer, { isFromGroup(it) }, true)
                    if (groupIds == "") {
                        dialogs.addAll(newDialogs)
                        view?.onDialogsLoaded(newDialogs)
                    } else {
                        loadGroups(groupIds)
                    }
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    private fun getAllIds(messages: MutableList<Message>,
                          filter: (Message) -> Boolean, isGroup: Boolean = false)
            = messages
            .filter(filter)
            .map { "${it.userId * (if (isGroup) -1 else 1)}" }
            .joinToString(separator = ",")

    private fun loadGroups(groupIds: String) {
        api.getGroups(groupIds)
                .subscribeSmart({
                    response ->
                    response.map { groups.put(it.id, it) }
                    val newDialogs = MutableList(
                            dialogsBuffer.size,
                            { index -> dialogsBuffer.map { if (isFromGroup(it)) fillDialogGroup(it) else it }[index] }
                    )
                    dialogs.addAll(newDialogs)
                    view?.onDialogsLoaded(newDialogs)
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    private fun setCounters(container: MessageContainer): Message {
        val dialog = container.message
        dialog?.unread = container.unread
        return dialog!!
    }

    private fun fillDialogUser(dialog: Message): Message {
        val user = users[dialog.userId]
        dialog.title = user?.fullName() ?: "ERR"
        dialog.photo = user?.photo100
        dialog.online = user?.online ?: 0
        dialog.isMute = Prefs.muteList.contains(getPollFrom(dialog.userId, dialog.chatId))
        return dialog
    }

    private fun fillDialogGroup(dialog: Message): Message {
        val group = groups[-dialog.userId]
        dialog.title = group?.name
        dialog.photo = group?.photo100
        return dialog
    }

    private fun isFromUser(dialog: Message) = (dialog.title?.equals(" ... ") ?: false || dialog.title?.equals("") ?: false) && dialog.userId > 0

    private fun isFromGroup(dialog: Message) = (dialog.title?.equals(" ... ") ?: false || dialog.title?.equals("") ?: false) && dialog.userId < 0
}