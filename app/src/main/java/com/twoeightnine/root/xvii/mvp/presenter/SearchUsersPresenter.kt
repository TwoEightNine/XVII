package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.SearchUsersFragmentView
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.ListResponse
import com.twoeightnine.root.xvii.utils.subscribeSmart
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction

typealias UserResponse = BaseResponse<ListResponse<User>>

class SearchUsersPresenter(api: ApiService): BasePresenter<SearchUsersFragmentView>(api) {

    private val userIdPattern = Regex("""^(id)?\d{1,9}$""")
    private val count = 100

    private var query = ""
    private val users: MutableList<User> = mutableListOf()

    fun getSaved() = users

    fun searchUsers(q: String = query, offset: Int = 0) {
        if (q == "") return
        query = q
        view?.showLoading()
        if (offset == 0) {
            users.clear()
        }
        val parsedUid = getUidFromQuery(query)
        if (parsedUid in 1..1000000000) {
            api.getUsers("$parsedUid", User.FIELDS)
                    .subscribeSmart({
                        response ->
                        if (offset == 0) {
                            view?.onUsersClear()
                        }
                        users.addAll(response)
                        view?.onUsersLoaded(response)
                    }, {
                        view?.showError(it)
                    })
        } else {
            Flowable.zip(
                    api.searchFriends(q, User.FIELDS, count, offset),
                    api.searchUsers(q, User.FIELDS, count, offset),
                    ResponseCombinerFunction())
                    .subscribeSmart({ response ->
                        if (offset == 0) {
                            view?.onUsersClear()
                        }
                        users.addAll(response.items)
                        view?.onUsersLoaded(response.items)
                    }, {
                        view?.showError(it)
                    })
        }
    }

    private fun getUidFromQuery(q: String)
        = if (userIdPattern.matches(q)) {
            try {
                userIdPattern.find(q)
                        ?.value
                        ?.replace("id", "")
                        ?.toInt() ?: 0
            } catch (e: NumberFormatException) {
                0
            }
        } else {
            0
        }

    private inner class ResponseCombinerFunction :
            BiFunction<UserResponse, UserResponse, UserResponse> {

        override fun apply(t1: UserResponse, t2: UserResponse): UserResponse {
            t1.response?.items?.addAll(t2.response?.items ?: arrayListOf())
            return t1
        }
    }

}