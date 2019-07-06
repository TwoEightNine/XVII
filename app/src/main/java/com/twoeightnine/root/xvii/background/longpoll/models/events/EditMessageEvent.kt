package com.twoeightnine.root.xvii.background.longpoll.models.events

class EditMessageEvent(
        id: Int,
        flags: Int,
        peerId: Int,
        timeStamp: Int,
        text: String,
        info: MessageInfo
) : BaseMessageEvent(id, flags, peerId, timeStamp, text, info) {

    override fun getType() = TYPE_EDIT_MESSAGE
}