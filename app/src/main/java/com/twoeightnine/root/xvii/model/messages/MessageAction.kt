package com.twoeightnine.root.xvii.model.messages

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.User
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageAction(

        @SerializedName("type")
        @Expose
        val type: String,

        @SerializedName("text")
        @Expose
        val text: String?,

        @SerializedName("message")
        @Expose
        val message: String? = null,

        @SerializedName("member_id")
        @Expose
        val memberId: Int? = 0,

        //manually added
        /**
         * who made this action
         */
        var actioner: User? = null,

        /**
         * who left the chat
         */
        var left: User? = null
) : Parcelable {

    fun getSystemMessage(context: Context): String? {
        val actionerName = actioner?.fullName?.toLowerCase() ?: ""
        return when (type) {
            TYPE_NEW_TITLE -> context.getString(R.string.chat_title_updated, actionerName, text ?: "")
            TYPE_NEW_PHOTO -> context.getString(R.string.chat_photo_updated, actionerName)
            TYPE_KICKED -> {
                if (actioner == left) { // user leaved
                    context.getString(R.string.user_leaved_chat, actionerName)
                } else { // user kicked
                    context.getString(R.string.user_kicked_user, actionerName, left?.fullName?.toLowerCase() ?: "")
                }
            }
            TYPE_PIN_MESSAGE -> context.getString(R.string.user_pinned_message, actionerName, message ?: "")
            else -> null
        }
    }

    companion object {

        const val TYPE_KICKED = "chat_kick_user"
        const val TYPE_NEW_TITLE = "chat_title_update"
        const val TYPE_NEW_PHOTO = "chat_photo_update"
        const val TYPE_PIN_MESSAGE = "chat_pin_message"
    }
}