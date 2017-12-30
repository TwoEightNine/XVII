package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.utils.loadUrl
import com.twoeightnine.root.xvii.utils.secToTime


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
        holder.tvDuration.text = secToTime(video.duration)
        holder.ivVideo.loadUrl(video.maxPhoto)
        holder.ivVideo.setOnClickListener {
            listener?.invoke(video)
        }
        holder.tvTitle.text = video.title
        return view
    }

    inner class VideoViewHolder(view: View) {

        @BindView(R.id.ivVideo)
        lateinit var ivVideo: ImageView
        @BindView(R.id.tvDuration)
        lateinit var tvDuration: TextView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }
}
