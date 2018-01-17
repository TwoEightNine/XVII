package com.twoeightnine.root.xvii.mvp.view

import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.mvp.BaseView

interface ChatFragmentView: BaseView {
    fun onHistoryLoaded(history: MutableList<Message>)
    fun onMessageAdded(message: Message)
    fun onHistoryClear()
    fun onSentError(text: String)
    fun onShowTyping()
    fun onHideTyping()
    fun onChangeOnline(isOnline: Boolean)
    fun onReadOut(mid: Int)
    fun onKeyGenerating()
    fun onKeySent()
    fun onKeyReceived(key: String, isWaiting: Boolean)
    fun onKeysExchanged()
    fun onKeyExchangeFailed()
    fun onMessagesDeleted(mids: MutableList<Int>)
    fun onMessageEdited(mid: Int, newText: String)
    fun onPhotoUploaded(path: String)
    fun onCacheRestored()

}