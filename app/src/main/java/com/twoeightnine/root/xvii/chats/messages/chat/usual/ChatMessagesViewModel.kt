package com.twoeightnine.root.xvii.chats.messages.chat.usual

import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesViewModel
import com.twoeightnine.root.xvii.network.ApiService

class ChatMessagesViewModel(api: ApiService) : BaseChatMessagesViewModel(api) {

    override fun prepareTextOut(text: String?) = text ?: ""

    override fun prepareTextIn(text: String) = text

    override fun preparePhoto(path: String, onPrepared: (String) -> Unit) {
        onPrepared(path)
    }
}