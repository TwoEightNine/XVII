package com.twoeightnine.root.xvii.chats.attachments.photos

import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart

class PhotoAttachViewModel(private val api: ApiService) : BaseAttachViewModel<Photo>() {

    override fun loadAttach(offset: Int) {
        api.getPhotos(Session.uid, "saved", COUNT, offset)
                .subscribeSmart({ response ->
                    onAttachmentsLoaded(offset, ArrayList(response.items))
                }, ::onErrorOccurred)
    }
}