package com.twoeightnine.root.xvii.dialogs2.models.api

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.isSticker
import com.twoeightnine.root.xvii.model.photosCount
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Message2(

        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("peer_id")
        @Expose
        val peerId: Int,

        @SerializedName("date")
        @Expose
        val date: Int,

        @SerializedName("from_id")
        @Expose
        val fromId: Int,

        @SerializedName("text")
        @Expose
        val text: String,

        @SerializedName("out")
        @Expose
        val out: Int,

        @SerializedName("fwd_messages")
        @Expose
        val fwdMessages: ArrayList<Message>? = arrayListOf(),

        @SerializedName("attachments")
        @Expose
        val attachments: ArrayList<Attachment>? = arrayListOf(),

        // ------------------- manually added values
        @SerializedName("read")
        @Expose
        var read: Boolean = false,

        @SerializedName("name")
        @Expose
        var name: String? = null,

        @SerializedName("photo")
        @Expose
        var photo: String? = null
) : Parcelable {

    fun isOut() = out == 1

    fun isSticker() = attachments != null && attachments.isSticker()

    fun hasPhotos() = attachments != null && attachments.photosCount() > 0

    fun getResolvedMessage(context: Context?) = when {
        context == null || text.isNotBlank() -> text
        attachments != null && attachments.isSticker() -> context.getString(R.string.sticker)
        attachments != null && attachments.isNotEmpty() -> {
            val count = attachments.size
            with(context.resources) {
                if (count == 1) {
                    getQuantityString(R.plurals.attachments, count)
                } else {
                    getQuantityString(R.plurals.attachments, count, count)
                }
            }
        }
        fwdMessages != null && fwdMessages.isNotEmpty() -> {
            val count = fwdMessages.size
            with(context.resources) {
                if (count == 1) {
                    getQuantityString(R.plurals.fwd_messages, count)
                } else {
                    getQuantityString(R.plurals.fwd_messages, count, count)
                }
            }
        }
        else -> text
    }
}