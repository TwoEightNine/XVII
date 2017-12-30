package com.twoeightnine.root.xvii.mvp.view

import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BaseView

interface FriendsFragmentView: BaseView {
    fun onUsersClear()
    fun onFriendsLoaded(friends: MutableList<User>)
    fun onOnlineFriendsLoaded(friends: MutableList<User>)
    fun onChatCreated()
}