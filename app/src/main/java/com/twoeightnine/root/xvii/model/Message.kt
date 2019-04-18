package com.twoeightnine.root.xvii.model

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.isSticker
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Message(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("date")
        @Expose
        val date: Int = 0,

        @SerializedName("out")
        @Expose
        val out: Int = 0,

        @SerializedName("user_id")
        @Expose
        var userId: Int = 0,

        @SerializedName("read_state")
        @Expose
        var readState: Int = 0,

        @SerializedName("title")
        @Expose
        var title: String? = null,

        @SerializedName("body")
        @Expose
        var body: String = "",

        @SerializedName("fwd_messages")
        @Expose
        var fwdMessages: ArrayList<Message>? = null,

        @SerializedName("attachments")
        @Expose
        val attachments: ArrayList<Attachment>? = null,

        @SerializedName("action")
        @Expose
        val action: String? = null,

        @SerializedName("action_mid")
        @Expose
        val actionMid: String? = null,

        @SerializedName("action_text")
        @Expose
        val actionText: String? = null,

        @SerializedName("emoji")
        @Expose
        val emoji: Int = 0,

        @SerializedName("chat_id")
        @Expose
        var chatId: Int = 0,

        @SerializedName("photo_100")
        @Expose
        var photo: String? = null,

        @SerializedName("chat_active")
        val chatActive: ArrayList<Int> = arrayListOf()
) : Parcelable {

    constructor(messageDb: MessageDb) : this(
            id = messageDb.id,
            date = messageDb.date,
            out = messageDb.out,
            userId = messageDb.userId,
            readState = messageDb.readState,
            title = messageDb.title,
            body = messageDb.body,
            emoji = messageDb.emoji,
            photo = messageDb.photo,
            chatId = messageDb.chatId,
            fwdMessages = Gson().fromJson(messageDb.fwdMessages, (object : TypeToken<MutableList<Message>>() {}).type),
            attachments = Gson().fromJson(messageDb.attachments, (object : TypeToken<MutableList<Attachment>>() {}).type)
    )

    val isOut: Boolean
        get() = out == 1

    var isRead: Boolean
        get() = readState == 1
        set(value) {
            readState = if (value) 1 else 0
        }

    companion object {

        val stubLoad: Message = Message(-1)

        val stubTry: Message = Message(-2)

        const val OUT_OF_CHAT = "chat_kick_user"
        const val IN_CHAT = "chat_invite_user"
        const val TITLE_UPDATE = "chat_title_update"
        const val CREATE = "chat_create"


        fun isStubLoad(mess: Message) = mess.id == -1

        fun isStubTry(mess: Message) = mess.id == -2
    }
}

fun Message.isSticker() = attachments?.isSticker() == true

fun Message.isSinglePhoto() = !attachments.isNullOrEmpty()
        && attachments[0].type == Attachment.TYPE_PHOTO
        && fwdMessages.isNullOrEmpty()