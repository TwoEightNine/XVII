package com.twoeightnine.root.xvii.background.longpoll.models.events

import com.google.gson.internal.LinkedTreeMap

data class NewMessageEvent(
        val id: Int,
        private val flags: Int,
        val peerId: Int,
        val timeStamp: Int,
        val text: String,
        val info: MessageInfo
) : BaseLongPollEvent() {

    companion object {
        const val FLAG_UNREAD = 1
        const val FLAG_OUT = 2
    }

    val title
        get() = info.title

    override fun getType() = TYPE_NEW_MESSAGE

    fun isUnread() = (flags and FLAG_UNREAD) > 0

    fun isOut() = (flags and FLAG_OUT) > 0

    fun hasMedia() = info.attachmentsCount > 0 || info.getForwardedCount() > 0

    fun isUser() = peerId in 0..2000000000

    fun hasEmoji() = info.emoji

    data class MessageInfo(
            val title: String = "",
            val from: Int = 0,
            val emoji: Boolean = false,
            val forwarded: String = "",
            val attachmentsCount: Int = 0
    ) {
        companion object {
            fun fromLinkedTreeMap(data: LinkedTreeMap<String, Any>): MessageInfo {
                var attachmentsCount = 0
                for (i in 10 downTo 1) {
                    if ("attach$i" in data) {
                        attachmentsCount = i
                        break
                    }
                }
                return MessageInfo(
                        title = (data["title"] as? String) ?: "",
                        from = (data["from"] as? Int) ?: 0,
                        emoji = (data["emoji"] as? String) == "1",
                        forwarded = (data["fwd"] as? String) ?: "",
                        attachmentsCount = attachmentsCount
                )
            }
        }

        fun getForwardedCount() = if (forwarded.isEmpty()) 0 else forwarded.split(",").size
    }
}