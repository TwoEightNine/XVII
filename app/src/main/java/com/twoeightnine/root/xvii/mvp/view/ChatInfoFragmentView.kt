package com.twoeightnine.root.xvii.mvp.view

import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BaseView

interface ChatInfoFragmentView: BaseView {
    fun onUsersLoaded(users: MutableList<User>)
    fun onChatRenamed(title: String)
    fun onUserLeft()
}