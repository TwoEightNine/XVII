package com.twoeightnine.root.xvii.mvp.view

import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BaseView

interface SearchUsersFragmentView: BaseView {

    fun onUsersLoaded(users: MutableList<User>)
    fun onUsersClear()

}