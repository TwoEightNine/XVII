package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.SearchMessagesFragmentView
import com.twoeightnine.root.xvii.utils.subscribeSmart

class SearchMessagesFragmentPresenter(api: ApiService): BasePresenter<SearchMessagesFragmentView>(api) {

    var COUNT_HISTORY = 40

    var query = ""
        private set

    var dialogsSearched: MutableList<Message> = mutableListOf()
    var dialogsBuffer: MutableList<Message> = mutableListOf()

    var withClear: Boolean = false

    fun getSavedSearch() = dialogsSearched

    fun searchDialogs(q: String = query, offset: Int = 0, withClear: Boolean = false) {
        if (q == "") return
        query = q
        view?.showLoading()
        if (offset == 0) {
            dialogsSearched.clear()
        }
        api.searchDialogs(Session.token, q, COUNT_HISTORY, "photo_100")
                .subscribeSmart({
                    response ->
                    dialogsBuffer.clear()
                    response.forEach {
                        dialogsBuffer.add(it.message)
                    }
//                    val userIds = getAllIds(dialogsBuffer, { isFromUser(it) })
//                    if (userIds != "") {
//                        loadUsers(userIds)
//                    } else {
//                        view?.onDialogsLoaded(mutableListOf())
//                    }
                    dialogsSearched.addAll(dialogsBuffer)
                    if (offset == 0) {
                        view?.onDialogsClear()
                    }
                    view?.onDialogsLoaded(dialogsBuffer)
                }, {
                    error ->
                    view?.showError(error)
                })
    }

}