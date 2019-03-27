package com.twoeightnine.root.xvii.background.longpoll.models.events

data class TypingChatEvent(val peerId: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_TYPING_CHAT
}