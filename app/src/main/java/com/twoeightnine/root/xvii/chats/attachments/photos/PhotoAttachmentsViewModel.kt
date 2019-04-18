package com.twoeightnine.root.xvii.chats.attachments.photos

import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsViewModel
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.network.ApiService

class PhotoAttachmentsViewModel(api: ApiService) : BaseAttachmentsViewModel<Photo>(api) {

    override val mediaType = "photo"

    override fun convert(attachment: Attachment?) = attachment?.photo
}