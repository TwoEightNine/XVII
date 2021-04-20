package com.twoeightnine.root.xvii.background.longpoll.models.events

class NewMessageEvent(
        id: Int,
        flags: Int,
        peerId: Int,
        timeStamp: Int,
        text: String,
        info: MessageInfo,
        randomId: Int
) : BaseMessageEvent(id, flags, peerId, timeStamp, text, info, randomId) {

    override fun getType() = TYPE_NEW_MESSAGE
}