package com.twoeightnine.root.xvii.music

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter

class TracksAdapter: SimpleAdapter<Track>() {

    override fun getView(pos: Int, v: View?, p2: ViewGroup?): View {
        val track = items[pos]
        var view = v
        if (view == null) {
            view = View.inflate(App.context, R.layout.container_audio, null)
            view.tag = TrackViewHolder(view)
        }
        val holder = view!!.tag as TrackViewHolder

        holder.tvTitle.text = track.title
        holder.tvArtist.text = track.artist
        return view
    }

    inner class TrackViewHolder(view: View) {

        @BindView(R.id.ivButton)
        lateinit var ivButton: ImageView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvArtist)
        lateinit var tvArtist: TextView

        init {
            ButterKnife.bind(this, view)
        }

    }
}