package com.twoeightnine.root.xvii.chats.attachments.videos

import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart

class VideoAttachViewModel(private val api: ApiService) : BaseAttachViewModel<Video>() {

    override fun loadAttach(offset: Int) {
        api.getVideos("", "", COUNT, offset)
                .subscribeSmart({ response ->
                    onAttachmentsLoaded(offset, ArrayList(response.items))
                }, ::onErrorOccurred)
    }
}