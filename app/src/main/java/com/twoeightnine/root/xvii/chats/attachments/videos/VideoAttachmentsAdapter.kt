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

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsAdapter
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.utils.secToTime
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_attachments_video.view.*

class VideoAttachmentsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Video) -> Unit
) : BaseAttachmentsAdapter<Video, VideoAttachmentsAdapter.VideoViewHolder>(context, loader) {

    override fun getViewHolder(view: View) = VideoViewHolder(view)

    override fun getLayoutId() = R.layout.item_attachments_video

    override fun createStubLoadItem() = Video()

    inner class VideoViewHolder(view: View)
        : BaseAttachmentsAdapter.BaseAttachmentViewHolder<Video>(view) {

        override fun bind(item: Video) {
            with(itemView) {
                tvDuration.setVisible(item.duration != 0)
                tvDuration.text = secToTime(item.duration)
                ivVideo.load(item.maxPhoto)
                tvTitle.text = item.title
                setOnClickListener { onClick(item) }
            }
        }
    }
}