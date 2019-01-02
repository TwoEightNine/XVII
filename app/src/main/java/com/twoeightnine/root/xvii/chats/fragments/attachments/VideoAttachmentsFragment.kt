package com.twoeightnine.root.xvii.chats.fragments.attachments

import android.widget.ListView
import butterknife.BindView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.VideoAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.response.AttachmentsResponse
import com.twoeightnine.root.xvii.utils.ApiUtils

class VideoAttachmentsFragment : BaseAttachmentsFragment<Video>() {

    @BindView(R.id.lvVideos)
    lateinit var lvVideos: ListView

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
