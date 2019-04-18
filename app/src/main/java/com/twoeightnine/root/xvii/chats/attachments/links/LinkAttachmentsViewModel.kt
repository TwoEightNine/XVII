package com.twoeightnine.root.xvii.chats.attachments.links

import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsViewModel
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Link
import com.twoeightnine.root.xvii.network.ApiService

class LinkAttachmentsViewModel(api: ApiService) : BaseAttachmentsViewModel<Link>(api) {

    override val mediaType = "link"

    override fun convert(attachment: Attachment?) = attachment?.link
}