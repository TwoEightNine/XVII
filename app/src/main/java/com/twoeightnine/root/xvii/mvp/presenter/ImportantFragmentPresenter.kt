package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.ImportantFragmentView
import com.twoeightnine.root.xvii.utils.subscribeSmart
import java.util.*

class ImportantFragmentPresenter(api: ApiService): BasePresenter<ImportantFragmentView>(api) {

    val COUNT = 200

    lateinit var dialog: Message
    val messages: MutableList<Message> = mutableListOf()
    val users: HashMap<Int, User> = hashMapOf()

    fun loadHistory(offset: Int = 0, withClear: Boolean = false) {
        view?.showLoading()
        if (offset == 0) {
            messages.clear()
        }
        api.getImportantMessages(COUNT, offset)
                .compose(applySchedulers())
                .subscribeSmart({
                    response ->
                    val history = response.items
                    loadUsers(history, withClear)
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    fun getSaved() = messages

    fun loadUsers(history: MutableList<Message>, withClear: Boolean = false) {
        val userIds = getAllIds(history)
        if (userIds.isEmpty()) {
            history
                    .map { it -> setMessageTitles(it, 0) }
                    .toMutableList()
            if (withClear) {
                view?.onHistoryClear()
            }
            messages.addAll(0, history)
            view?.onHistoryLoaded(history)
            return
        }
        api.getUsers(userIds, User.FIELDS)
                .subscribeSmart({
                    response ->
                    response.map { users.put(it.id, it) }
                    history
                            .map { it -> setMessageTitles(it, 0) }
                            .toMutableList()
                    if (withClear) {
                        view?.onHistoryClear()
                    }
                    messages.addAll(0, history)
                    view?.onHistoryLoaded(history)
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    fun getAllIds(messes: MutableList<Message>): String {
        val ids = HashSet<String>()
        ids.add("${Session.uid}")
        for (i in messes.indices) {
            if (!users.containsKey(messes[i].userId)) {
                ids.add("${messes[i].userId}")
            }
            if (messes[i].fwdMessages != null) {
                ids.add(getAllIds(messes[i].fwdMessages!!))
            }
        }
        return "${ids.joinToString(separator = ",")},"
    }

    fun deleteMessages(mids: MutableList<Int>) {
        api.deleteMessages(mids.joinToString(separator = ","))
                .subscribeSmart({
                    view?.onMessagesDeleted(mids)
                }, {})
    }

    fun setMessageTitles(message: Message, level: Int): Message {
        val user = this.users[message.userId]
        if (user != null) {
            message.title = user.fullName()
            message.photo = user.photo100
        }
        if (message.fwdMessages != null) {
            val fwd = message.fwdMessages ?: arrayListOf<Message>()
            for (i in fwd.indices) {
                fwd[i] = setMessageTitles(fwd[i], level + 1)
            }
            message.fwdMessages = fwd
        }
        return message
    }

}