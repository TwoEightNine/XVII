package com.twoeightnine.root.xvii.chats.fragments.attachments

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.VideoAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.network.response.AttachmentsResponse
import kotlinx.android.synthetic.main.fragment_attachments_video.*

class VideoAttachmentsFragment : BaseAttachmentsFragment<Video>() {

    override fun getLayout() = R.layout.fragment_attachments_video

    override fun getMedia() = "video"

    override fun initAdapter() {
        App.appComponent?.inject(this)
        adapter = VideoAttachmentsAdapter({ loadMore() }, { apiUtils.openVideo(safeActivity, it) })
        lvVideos.adapter = adapter
    }

    override fun onLoaded(response: AttachmentsResponse) {
        adapter.stopLoading(response.items
                .map { it.attachment?.video!! }
                .toMutableList())
    }

    companion object {

        fun newInstance(peerId: Int): VideoAttachmentsFragment {
            val frag = VideoAttachmentsFragment()
            frag.peerId = peerId
            return frag
        }
    }
}
