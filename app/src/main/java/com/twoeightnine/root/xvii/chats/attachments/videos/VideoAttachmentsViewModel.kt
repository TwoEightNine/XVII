package com.twoeightnine.root.xvii.chats.attachments.videos

import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsViewModel
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart

class VideoAttachmentsViewModel(api: ApiService) : BaseAttachmentsViewModel<Video>(api) {

    override val mediaType = "video"

    override fun convert(attachment: Attachment?) = attachment?.video

    fun loadVideoPlayer(
            video: Video,
            onPlayerLoader: (String) -> Unit,
            onError: (String?) -> Unit
    ) {
        api.getVideos(video.videoId, video.accessKey)
                .subscribeSmart({ response ->
                    val player = response.items.getOrNull(0)?.player
                    if (player != null) {
                        onPlayerLoader(player)
                    } else {
                        onError(null)
                    }
                }, onError)
    }
}