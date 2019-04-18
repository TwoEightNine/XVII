package com.twoeightnine.root.xvii.chats.attachments.videos

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsAdapter
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.utils.loadRounded
import com.twoeightnine.root.xvii.utils.secToTime
import com.twoeightnine.root.xvii.utils.setVisible
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
                ivVideo.loadRounded(item.maxPhoto)
                tvTitle.text = item.title
                setOnClickListener { onClick(item) }
            }
        }
    }
}