package com.twoeightnine.root.xvii.background.longpoll.models.events

data class TypingEvent(val userId: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_TYPING
}