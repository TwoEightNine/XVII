package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.attachments.Attachment


class AttachmentsResponse {

    val items: MutableList<AttachmentsContainer> = mutableListOf()
    @SerializedName("next_from")
    val nextFrom: String? = null

    inner class AttachmentsContainer {
        @SerializedName("message_id")
        var messageId: Int = 0
        var attachment: Attachment? = null
    }

}
