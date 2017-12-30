package com.twoeightnine.root.xvii.mvp.view

import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.BaseView

interface DialogFwFragmentView: BaseView {
    fun onDialogsLoaded(dialogs: MutableList<Message>)
}