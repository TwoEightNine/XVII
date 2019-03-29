package com.twoeightnine.root.xvii.background.longpoll.models.events

data class DeleteMessagesEvent(val peerId: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_DELETE_MESSAGES
}