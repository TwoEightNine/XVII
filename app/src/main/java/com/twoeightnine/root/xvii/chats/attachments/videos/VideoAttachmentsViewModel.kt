/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.chats.attachments.videos

import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsViewModel
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Video
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