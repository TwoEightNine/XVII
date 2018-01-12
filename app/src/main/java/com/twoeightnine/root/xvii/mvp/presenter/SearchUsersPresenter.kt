package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.SearchUsersFragmentView
import com.twoeightnine.root.xvii.utils.subscribeSmart

class SearchUsersPresenter(api: ApiService): BasePresenter<SearchUsersFragmentView>(api) {

    val COUNT = 40

    var query = ""
        private set

    val users: MutableList<User> = mutableListOf()

    fun getSaved() = users

    fun searchUsers(q: String = query, offset: Int = 0) {
        if (q == "") return
        query = q
        view?.showLoading()
        if (offset == 0) {
            users.clear()
        }
        api.search(q, User.FIELDS, COUNT, offset)
                .subscribeSmart({
                    response ->
                    if (offset == 0) {
                        view?.onUsersClear()
                    }
                    users.addAll(response.items)
                    view?.onUsersLoaded(response.items)
                }, {
                    error ->
                    view?.showError(error)
                })
    }

}