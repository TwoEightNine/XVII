package com.twoeightnine.root.xvii.model

import android.content.Context
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.utils.shortifyNumber

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

    override fun getInfoText(context: Context): String {
        val count = chatSettings?.membersCount ?: 0
        val number = shortifyNumber(count)
        return context.resources.getQuantityString(R.plurals.participants, count, number)
    }

    override fun getPrivacyInfo(context: Context): String? = when {
        canWrite?.allowed == false -> context.getString(R.string.unable_to_write)
        else -> null
    }

    fun isRead() = inRead == outRead

    companion object {
        const val FIELDS = ""
    }
}

data class ChatSettings(

        @SerializedName("title")
        @Expose
        val title: String? = "",

        @SerializedName("photo")
        @Expose
        val photo: ChatPhoto,

        @SerializedName("members_count")
        val membersCount: Int = 0,

        @SerializedName("active_ids")
        val activeIds: List<Int> = arrayListOf(),

        @SerializedName("pinned_message")
        val pinnedMessage: Message? = null
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