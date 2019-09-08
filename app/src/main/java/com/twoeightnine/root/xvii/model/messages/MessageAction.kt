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
         * the subject of the action
         */
        var subject: User? = null,

        /**
         * the object of the action
         */
        var objekt: User? = null
) : Parcelable {

    fun getSystemMessage(context: Context): String? {
        val subjectName = subject?.fullName?.toLowerCase() ?: ""
        val objektName = objekt?.fullName?.toLowerCase() ?: ""
        return when (type) {
            TYPE_NEW_TITLE -> context.getString(R.string.chat_title_updated, subjectName, text ?: "")
            TYPE_NEW_PHOTO -> context.getString(R.string.chat_photo_updated, subjectName)
            TYPE_REMOVE_PHOTO -> context.getString(R.string.chat_photo_removed, subjectName)
            TYPE_KICKED -> {
                if (subject == objekt) { // user leaved
                    context.getString(R.string.user_leaved_chat, subjectName)
                } else { // user kicked
                    context.getString(R.string.user_kicked_user, subjectName, objektName)
                }
            }
            TYPE_INVITED -> if (subject == objekt) { // user entered
                context.getString(R.string.user_entered_chat, subjectName)
            } else { // user invited a user
                context.getString(R.string.user_invited_user, subjectName, objektName)
            }
            TYPE_INVITED_BY_LINK -> if (subject == objekt) { // user entered
                context.getString(R.string.user_entered_chat_via_link, subjectName)
            } else { // user invited a user
                context.getString(R.string.user_invited_user_via_link, subjectName, objektName)
            }
            TYPE_PIN_MESSAGE -> context.getString(R.string.user_pinned_message, subjectName, message ?: "")
            TYPE_UNPIN_MESSAGE -> context.getString(R.string.user_unpinned_message, subjectName, message ?: "")
            TYPE_NEW_CHAT -> context.getString(R.string.chat_created, subjectName)
            else -> null
        }
    }

    companion object {

        const val TYPE_KICKED = "chat_kick_user"
        const val TYPE_INVITED = "chat_invite_user"
        const val TYPE_INVITED_BY_LINK = "chat_invite_user_by_link"
        const val TYPE_NEW_TITLE = "chat_title_update"
        const val TYPE_NEW_CHAT = "chat_create"
        const val TYPE_NEW_PHOTO = "chat_photo_update"
        const val TYPE_REMOVE_PHOTO = "chat_photo_remove"
        const val TYPE_PIN_MESSAGE = "chat_pin_message"
        const val TYPE_UNPIN_MESSAGE = "chat_unpin_message"
    }
}