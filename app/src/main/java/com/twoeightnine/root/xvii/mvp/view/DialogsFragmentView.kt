package com.twoeightnine.root.xvii.mvp.view

import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.BaseView

interface DialogsFragmentView : BaseView {
    fun onDialogsLoaded(dialogs: MutableList<Message>)
    fun onDialogsClear()
    fun onRemoveDialog(position: Int)
    fun onMessageReceived(event: NewMessageEvent)
    fun onOnlineChanged(userId: Int, isOnline: Boolean)
    fun onMessageReadIn(userId: Int, mid: Int)
    fun onMessageReadOut(userId: Int, mid: Int)
    fun onCacheRestored()
    fun onMessageNew(message: Message)
}