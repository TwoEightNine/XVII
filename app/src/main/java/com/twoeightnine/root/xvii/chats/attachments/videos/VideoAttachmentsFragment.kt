package com.twoeightnine.root.xvii.chats.attachments.videos

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsFragment
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.web.VideoViewerActivity

class VideoAttachmentsFragment : BaseAttachmentsFragment<Video>() {

    override val adapter by lazy {
        VideoAttachmentsAdapter(requireContext(), ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun getViewModelClass() = VideoAttachmentsViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(video: Video) {
        (viewModel as? VideoAttachmentsViewModel)?.loadVideoPlayer(video, { player ->
            VideoViewerActivity.launch(context, player)
        }) { error ->
            showError(context, error ?: getString(R.string.error))
        }
    }

    companion object {
        fun newInstance(peerId: Int): VideoAttachmentsFragment {
            val fragment = VideoAttachmentsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}