package com.twoeightnine.root.xvii.model

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.utils.getPeerId
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class MessageDb(): RealmObject() {

    constructor(message: Message): this() {
        this.id = message.id
        this.userId = message.userId
        this.chatId = message.chatId
        this.peerId = getPeerId(userId, chatId)
        this.date = message.date
        this.out = message.out
        this.readState = message.readState
        this.title = message.title ?: ""
        this.body = message.body ?: ""
        this.emoji = message.emoji
        this.photo = message.photo

        val gson = Gson()
        this.fwdMessages = gson.toJson(message.fwdMessages)
        this.attachments = gson.toJson(message.attachments)

    }

    @PrimaryKey
    @SerializedName("id")
    @Expose
    open var id: Int = 0

    @SerializedName("date")
    @Expose
    open var date: Int = 0

    @SerializedName("out")
    @Expose
    open var out: Int = 0

    @SerializedName("user_id")
    @Expose
    open var userId: Int = 0

    @SerializedName("chat_id")
    @Expose
    open var chatId: Int = 0

    @SerializedName("peer_id")
    @Expose
    open var peerId: Int = 0

    @SerializedName("read_state")
    @Expose
    open var readState: Int = 0

    @SerializedName("title")
    @Expose
    open var title: String = ""

    @SerializedName("body")
    @Expose
    open var body: String = ""

    @SerializedName("fwd_messages")
    @Expose
    open var fwdMessages: String = ""

    @SerializedName("attachments")
    @Expose
    open var attachments: String = ""

    @SerializedName("emoji")
    @Expose
    open var emoji: Int = 0

    @SerializedName("photo")
    @Expose
    open var photo: String? = ""

    override fun toString() = "Message{id: $id, date: $date, userId: $userId, peerId: $peerId, photo: $photo, body: $body, fwd: $fwdMessages, attachments: $attachments}"
}