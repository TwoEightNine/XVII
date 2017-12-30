package com.twoeightnine.root.xvii.mvp.view

import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BaseView

interface ProfileFragmentView: BaseView {
    fun onUserLoaded(user: User)
    fun onFoafLoaded(date: String)
    fun onPhotosLoaded(photos: MutableList<Photo>)
}