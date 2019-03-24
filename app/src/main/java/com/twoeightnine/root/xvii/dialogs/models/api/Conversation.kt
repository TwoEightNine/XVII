package com.twoeightnine.root.xvii.dialogs.models.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Conversation(

    @SerializedName("chat_settings")
    @Expose
    val chatSettings: ChatSettings? = null,

    @SerializedName("unread_count")
    @Expose
    val unreadCount: Int = 0,

    @SerializedName("in_read")
    @Expose
    val inRead: Int = 0,

    @SerializedName("out_read")
    @Expose
    val outRead: Int = 0,

    @SerializedName("can_write")
    @Expose
    val canWrite: CanWrite? = null
) {
    fun isRead() = inRead == outRead
}

data class ChatSettings(

    @SerializedName("title")
    @Expose
    val title: String = "",

    @SerializedName("photo")
    @Expose
    val photo: ChatPhoto
)

data class ChatPhoto(

    @SerializedName("photo_100")
    @Expose
    val photo100: String = ""
)

data class CanWrite(

    @SerializedName("allowed")
    @Expose
    val allowed: Boolean = true,

    @SerializedName("reason")
    @Expose
    val reason: Int = 0
)