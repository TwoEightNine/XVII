package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.FriendsFragmentView
import com.twoeightnine.root.xvii.utils.subscribeSmart

class FriendsFragmentPresenter(api: ApiService): BasePresenter<FriendsFragmentView>(api) {

    val COUNT = 200

    val friends: MutableList<User> = mutableListOf()
    val online: MutableList<User> = mutableListOf()

    fun loadFriends(offset: Int = 0) {
        view?.showLoading()
        api.getFriends(
                User.FIELDS,
                COUNT,
                offset
                )
                .subscribeSmart({
                    response ->
                    val loaded = response.items
                    friends.addAll(loaded)
                    val onlineLoaded = loaded
                            .filter { it.online == 1 }
                            .toMutableList()
                    online.addAll(onlineLoaded)
                    view?.hideLoading()
                    view?.onFriendsLoaded(loaded)
                    view?.onOnlineFriendsLoaded(onlineLoaded)
                }, {
                    error ->
                    view?.hideLoading()
                    view?.showError(error)
                })
    }

    fun searchUsers(q: String, offset: Int = 0) {
        view?.showLoading()
        api.searchUsers(q, User.FIELDS, COUNT, offset)
                .subscribeSmart({
                    response ->
                    if (offset == 0) {
                        view?.onUsersClear()
                    }
                    view?.onFriendsLoaded(response.items)
                }, {
                    error ->
                    view?.showError(error)
                })
    }

    fun loadUsers(users: MutableList<Int>) {
        view?.showLoading()
        val userIds = users.joinToString(separator = ",")
        api.getUsers(userIds, User.FIELDS)
                .subscribeSmart({
                    response ->
                    view?.hideLoading()
                    view?.onFriendsLoaded(response)
                }, {
                    error ->
                    view?.hideLoading()
                    view?.showError(error)
                })
    }

    fun createChat(userIds: String) {
        view?.showLoading()
        api.createChat(userIds)
                .subscribeSmart({
                    response ->
                    view?.hideLoading()
                    if (response != 0) {
                        view?.onChatCreated()
                    } else {
                        view?.showError("")
                    }
                }, {
                    error ->
                    view?.hideLoading()
                    view?.showError(error)
                })
    }

}