package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.utils.getPeerId
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class DialogDb(): RealmObject() {

    constructor(message: Message): this() {
        this.peerId = getPeerId(message.userId, message.chatId)
        this.date = message.date
        this.readState = message.readState
        this.title = message.title ?: ""
        this.body = message.body ?: ""
        this.emoji = message.emoji
        this.photo = message.photo ?: Api.PHOTO_STUB
    }

    @PrimaryKey
    @SerializedName("peer_id")
    @Expose
    open var peerId: Int = 0

    @SerializedName("date")
    @Expose
    open var date: Int = 0

    @SerializedName("read_state")
    @Expose
    open var readState: Int = 0

    @SerializedName("title")
    @Expose
    open var title: String = ""

    @SerializedName("body")
    @Expose
    open var body: String = ""

    @SerializedName("emoji")
    @Expose
    open var emoji: Int = 0

    @SerializedName("photo")
    @Expose
    open var photo: String = ""

    override fun toString() = "Message{peerId: $peerId, date: $date, photo: $photo, body: $body}"


}