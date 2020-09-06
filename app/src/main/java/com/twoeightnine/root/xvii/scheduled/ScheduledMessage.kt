package com.twoeightnine.root.xvii.scheduled

data class ScheduledMessage(

        val id: Int,

        val peerId: Int,

        val whenMs: Long,

        val text: String,

        val attachments: String? = null,

        val forwardedMessages: String? = null
)