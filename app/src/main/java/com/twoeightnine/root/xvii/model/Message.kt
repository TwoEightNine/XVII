package com.twoeightnine.root.xvii.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.twoeightnine.root.xvii.utils.getFromPeerId
import java.io.Serializable
import java.util.*

class Message : Parcelable, Serializable {

    @SerializedName("peer_id")
    val peerId: Int = 0
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("date")
    var date: Int = 0
    @SerializedName("out")
    var out: Int = 0
    @SerializedName("user_id")
    var userId: Int = 0
    @SerializedName("read_state")
    var readState: Int = 0
    @SerializedName("title")
    var title: String? = null
    @SerializedName("body")
    var body: String? = null
    @SerializedName("fwd_messages")
    var fwdMessages: ArrayList<Message>? = null
    @SerializedName("attachments")
    var attachments: ArrayList<Attachment>? = null
    var online: Int = 0
    val action: String? = null
    @SerializedName("action_mid")
    val actionMid: String? = null
    @SerializedName("action_text")
    val actionText: String? = null
    private var important: Int = 0
    @SerializedName("emoji")
    var emoji: Int = 0

    @SerializedName("chat_id")
    var chatId: Int = 0
    @SerializedName("photo_100")
    var photo: String? = null
    private val photoRes: Int? = null
    @SerializedName("chat_active")
    var chatActive: ArrayList<Int>? = null

    var unread: Int = 0
    var isMute: Boolean = false

    constructor()

    constructor(p: Parcel) {
        id = p.readInt()
        date = p.readInt()
        out = p.readInt()
        userId = p.readInt()
        readState = p.readInt()
        title = p.readString()
        body = p.readString()
        chatId = p.readInt()
        photo = p.readString()
        online = p.readInt()
        if (chatActive == null) {
            chatActive = ArrayList<Int>()
        }
        p.readList(chatActive, null)
    }

    constructor(messageDb: MessageDb) {
        this.id = messageDb.id
        this.date = messageDb.date
        this.out = messageDb.out
        this.userId = messageDb.userId
        this.readState = messageDb.readState
        this.title = messageDb.title
        this.body = messageDb.body
        this.emoji = messageDb.emoji
        this.photo = messageDb.photo
        this.chatId = messageDb.chatId

        val gson = Gson()
        this.fwdMessages = gson.fromJson(messageDb.fwdMessages, (object : TypeToken<MutableList<Message>>() {}).type)
        this.attachments = gson.fromJson(messageDb.attachments, (object : TypeToken<MutableList<Attachment>>() {}).type)
    }

    constructor(dialogDb: DialogDb) {
        val data = getFromPeerId(dialogDb.peerId)
        this.userId = data[0]
        this.chatId = data[1]
        this.date = dialogDb.date
        this.readState = dialogDb.readState
        this.title = dialogDb.title
        this.body = dialogDb.body
        this.emoji = dialogDb.emoji
        this.photo = dialogDb.photo
    }

    constructor(id: Int) {
        this.id = id
    }

    @JvmOverloads
    constructor(id: Int, date: Int, userId: Int,
                out: Int, readState: Int, title: String,
                body: String, attachments: ArrayList<Attachment>? = null,
                emoji: Int = 0) {
        this.id = id
        this.date = date
        this.userId = userId
        this.out = out
        this.readState = readState
        this.title = title
        this.body = body
        this.attachments = attachments
        this.emoji = emoji
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id)
        parcel.writeInt(date)
        parcel.writeInt(out)
        parcel.writeInt(userId)
        parcel.writeInt(readState)
        parcel.writeString(title)
        parcel.writeString(body)
        parcel.writeInt(chatId)
        parcel.writeString(photo)
        parcel.writeInt(online)
        parcel.writeList(chatActive)

    }

    val isOut: Boolean
        get() = out == 1

    val isRead: Boolean
        get() = readState == 1

    fun setRead(rs: Int) {
        readState = rs
    }

    val isImportant: Boolean
        get() = false

    fun getResolvedMessage(context: Context?) = body //TODO

    override fun toString() = "[id: $id, userId: $userId, chatId: $chatId, title: $title, photo: $photo, body: $body, readState: $readState, out: $out]"

    companion object {

        val stubLoad: Message = Message(-1)

        val stubTry: Message = Message(-2)

        const val OUT_OF_CHAT = "chat_kick_user"
        const val IN_CHAT = "chat_invite_user"
        const val TITLE_UPDATE = "chat_title_update"
        const val CREATE = "chat_create"


        fun isStubLoad(mess: Message) = mess.id == -1

        fun isStubTry(mess: Message) = mess.id == -2

        var CREATOR: Parcelable.Creator<Message> = object : Parcelable.Creator<Message> {
            override fun createFromParcel(parcel: Parcel): Message {
                return Message(parcel)
            }

            override fun newArray(i: Int): Array<Message?> {
                return arrayOfNulls(i)
            }
        }
    }
}