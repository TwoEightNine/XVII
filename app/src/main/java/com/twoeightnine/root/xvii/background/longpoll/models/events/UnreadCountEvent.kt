package com.twoeightnine.root.xvii.background.longpoll.models.events

data class UnreadCountEvent(val unreadCount: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_COUNT
}