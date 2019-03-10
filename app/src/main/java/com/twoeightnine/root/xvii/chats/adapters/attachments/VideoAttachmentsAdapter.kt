package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.utils.loadUrl
import com.twoeightnine.root.xvii.utils.secToTime
import kotlinx.android.synthetic.main.item_attachments_video.view.*


class VideoAttachmentsAdapter(loader: ((Int) -> Unit)?,
                              listener: ((Video) -> Unit)?) : SimplePaginationAdapter<Video>(loader, listener) {

    override fun getView(pos: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val video = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_attachments_video, null)
            view!!.tag = VideoViewHolder(view)
        }
        val holder = view.tag as VideoViewHolder
        holder.bind(video)
        return view
    }

    inner class VideoViewHolder(private val view: View) {

        fun bind(video: Video) {
            with(view) {
                tvDuration.text = secToTime(video.duration)
                ivVideo.loadUrl(video.maxPhoto)
                ivVideo.setOnClickListener {
                    listener?.invoke(video)
                }
                tvTitle.text = video.title
            }
        }
    }
}
