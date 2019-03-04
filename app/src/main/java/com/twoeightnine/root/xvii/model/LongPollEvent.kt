package com.twoeightnine.root.xvii.model

import com.google.gson.Gson
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.utils.getPeerId

/**
 * Created by root on 9/29/16.
 */

class LongPollEvent(upd: MutableList<Any>) {

    var type: Int = 0
    var userId: Int = 0
        private set
    var count: Int = 0
        private set
    var mid: Int = 0
        private set
    var flags: Int = 0
        private set
    var ts: Int = 0
        private set
    var chatId: Int = 0
        private set
    var title: String = ""
        private set
    var message: String = ""
        private set
    var obj: Any? = null
        private set
    var info: InfoObject? = null
        private set

    init {
        if (upd.size != 0) {
            try {
                type = (upd[0] as Double).toInt()
                when (type) {

                    NEW_MESSAGE -> {
                        mid = (upd[1] as Double).toInt()
                        flags = (upd[2] as Double).toInt()
                        userId = (upd[3] as Double).toInt()
                        ts = (upd[4] as Double).toInt()
                        title = upd[5] as String
                        message = upd[6] as String
                        obj = upd[7]
                        val gson = Gson()
                        try {
                            info = gson.fromJson(obj!!.toString(), InfoObject::class.java)
                        } catch (e: Exception) {
                            info = InfoObject()
                        }

                    }

                    NEW_COUNT -> count = (upd[1] as Double).toInt()

                    READ_IN, READ_OUT -> {
                        userId = (upd[1] as Double).toInt()
                        mid = (upd[2] as Double).toInt()
                    }

                    ONLINE, OFFLINE -> userId = -(upd[1] as Double).toInt()

                    TYPING_IN_USER, RECORDING_VOICE -> userId = (upd[1] as Double).toInt()

                    TYPING_IN_CHAT -> {
                        userId = (upd[1] as Double).toInt()
                        chatId = (upd[2] as Double).toInt()
                    }
                }
            } catch (e: Exception) {
                Lg.wtf("LongPollEvent error: $e")
                e.printStackTrace()
            }
        }

    }

    constructor(message: Message): this(mutableListOf()) {
        type = NEW_MESSAGE
        userId = getPeerId(message.userId, message.chatId)
        flags = if (message.isOut) 3 else 1
        ts = message.date
        title = message.title ?: ""
        this.message = message.body ?: ""
        obj = InfoObject(message)
    }

    fun getRawUpd(): MutableList<Any> {
        val res = mutableListOf<Any>()
        res.add(type)
        res.add(mid)
        res.add(flags)
        res.add(userId)
        res.add(ts)
        res.add(title)
        res.add(message)
        res.add(obj ?: InfoObject())
        return res
    }

    override fun toString() = "{type: $type, mid: $mid, user_id: $userId}"

    val out: Int
        get() = if (flags and 2 == 2) 1 else 0

    inner class InfoObject() {
        var from: Int = 0
        private var attach1_type: String? = null
        private val attach2_type: String? = null
        private val attach3_type: String? = null
        private val attach4_type: String? = null
        private val attach5_type: String? = null
        private val attach6_type: String? = null
        private val attach7_type: String? = null
        private val attach8_type: String? = null
        private val attach9_type: String? = null
        private val attach10_type: String? = null
        private var fwd: String? = null
        private var fwd_msg_count: String? = null
        private var emoji: String? = null

        constructor(message: Message): this() {
            if (message.emoji == 1) {
                emoji = "1"
            }
            if (message.attachments != null && message.attachments!!.size > 0) {
                attach1_type = "photo"
            }
            if (message.fwdMessages != null && message.fwdMessages!!.size > 0) {
                fwd = message.fwdMessages!!.joinToString(separator = ",")
                fwd_msg_count = message.fwdMessages!!.size.toString()
            }
            if (message.chatId != 0) {
                from = message.userId
            }
        }

        fun hasAttachments(): Boolean {
            return attach1_type != null || fwd != null
        }

        val hasEmojis: Boolean
            get() = if (emoji == null) false else emoji?.toInt() == 1

        val forwardedCount: Int
            get() = if (fwd_msg_count != null) Integer.parseInt(fwd_msg_count) else 0

        val attachmentsCount: Int
            get() {
                if (attach10_type != null)
                    return 10

                if (attach9_type != null)
                    return 9

                if (attach8_type != null)
                    return 8

                if (attach7_type != null)
                    return 7

                if (attach6_type != null)
                    return 6

                if (attach5_type != null)
                    return 5

                if (attach4_type != null)
                    return 4

                if (attach3_type != null)
                    return 3

                if (attach2_type != null)
                    return 2

                if (attach1_type != null)
                    return 1

                return 0
            }
    }

    companion object {

        const val NEW_MESSAGE = 4
        const val READ_IN = 6
        const val READ_OUT = 7
        const val ONLINE = 8
        const val OFFLINE = 9
        const val TYPING_IN_USER = 61
        const val TYPING_IN_CHAT = 62
        const val RECORDING_VOICE = 64
        const val NEW_COUNT = 80
    }

}
