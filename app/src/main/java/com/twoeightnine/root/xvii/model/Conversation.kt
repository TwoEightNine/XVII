package com.twoeightnine.root.xvii.model

import android.content.Context
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner

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

        @SerializedName("peer")
        @Expose
        val peer: Peer? = null,

        @SerializedName("can_write")
        @Expose
        val canWrite: CanWrite? = null
) : ChatOwner {

    override fun getPeerId() = peer?.id ?: 0

    override fun getAvatar() = chatSettings?.photo?.photo100

    override fun getTitle() = chatSettings?.title ?: ""

    override fun getInfoText(context: Context): String = context.getString(R.string.conversation)

    fun isRead() = inRead == outRead
}

data class ChatSettings(

        @SerializedName("title")
        @Expose
        val title: String? = "",

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

data class Peer(
        @SerializedName("id")
        @Expose
        val id: Int
)