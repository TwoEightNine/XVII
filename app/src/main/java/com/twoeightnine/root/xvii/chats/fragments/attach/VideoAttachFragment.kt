package com.twoeightnine.root.xvii.chats.fragments.attach

import android.widget.ListView
import butterknife.BindView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.attachments.VideoAttachmentsAdapter
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.utils.showCommon
import com.twoeightnine.root.xvii.utils.subscribeSmart

class VideoAttachFragment : BaseAttachFragment<Video>() {

    companion object {
        fun newInstance(listener: ((Attachment) -> Unit)?): VideoAttachFragment {
            val frag = VideoAttachFragment()
            frag.listener = listener
            return frag
        }
    }

    @BindView(R.id.lvVideos)
    lateinit var lvVideos: ListView

    override fun getLayout() = R.layout.fragment_attachments_video

    override fun initAdapter() {
        App.appComponent?.inject(this)
        adapter = VideoAttachmentsAdapter({ load() }, { listener?.invoke(Attachment(it)) })
        lvVideos.adapter = adapter
    }

    fun load() {
        api.getVideos(Session.token, "", "", count, adapter.count)
                .subscribeSmart({
                    response ->
                    adapter.stopLoading(response.items)
                }, {
                    showCommon(activity, it)
                    adapter.isLoading = false
                })
    }
}