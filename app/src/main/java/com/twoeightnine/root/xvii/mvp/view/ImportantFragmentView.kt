package com.twoeightnine.root.xvii.mvp.view

import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.BaseView

interface ImportantFragmentView: BaseView {
    fun onHistoryLoaded(history: MutableList<Message>)
    fun onHistoryClear()
    fun onMessagesDeleted(mids: MutableList<Int>)
}