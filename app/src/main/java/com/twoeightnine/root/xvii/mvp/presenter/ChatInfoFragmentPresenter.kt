package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.ChatInfoFragmentView
import com.twoeightnine.root.xvii.utils.subscribeSmart

class ChatInfoFragmentPresenter(api: ApiService): BasePresenter<ChatInfoFragmentView>(api) {

    lateinit var message: Message

    fun loadUsers() {
        view?.showLoading()
        val userIds = message.chatActive?.joinToString(separator = ",") ?: ""
        api.getUsers(userIds, User.FIELDS)
                .subscribeSmart({
                    response ->
                    view?.onUsersLoaded(response)
                }, {
                    view?.showError(it)
                })
    }

    fun leaveChat() {
        api.removeUser(message.chatId ,Session.uid)
                .subscribeSmart({
                    response ->
                    if (response == 1) {
                        view?.onUserLeft()
                    } else {
                        view?.showError(message.title ?: "")
                    }
                }, {
                    view?.showError(it)
                })
    }

    fun renameChat(title: String) {
        if (title == message.title) return
        api.renameChat(message.chatId, title)
                .subscribeSmart({
                    response ->
                    if (response == 1) {
                        view?.onChatRenamed(title)
                    } else {
                        view?.showError(message.title ?: "")
                    }
                }, {
                    view?.showError(it)
                })
    }

}