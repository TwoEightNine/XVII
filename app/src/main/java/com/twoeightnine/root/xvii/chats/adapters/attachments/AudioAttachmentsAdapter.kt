package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.utils.secToTime
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.show
import kotlinx.android.synthetic.main.item_attachments_track.view.*


class AudioAttachmentsAdapter(
        loader: ((Int) -> Unit)?,
        onClick: ((Track) -> Unit),
        private val onDownload: (Track) -> Unit
) : SimplePaginationAdapter<Track>(loader, onClick) {

    var played: Track? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getView(pos: Int, v: View?, viewGroup: ViewGroup): View {
        var view = v
        val track = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_attachments_track, null)
            view!!.tag = TrackViewHolder(view)
        }
        val holder = view.tag as TrackViewHolder
        holder.bind(track)
        return view
    }

    inner class TrackViewHolder(private val view: View) {

        fun bind(track: Track) {
            with(view) {
                val icon = if (track == played) {
                    val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)
                    Style.forDrawable(dPause, Style.DARK_TAG)
                    dPause
                } else {
                    val dPlay = ContextCompat.getDrawable(context, R.drawable.play_big)
                    Style.forDrawable(dPlay, Style.DARK_TAG)
                    dPlay
                }
                Style.forImageView(ivDownload, Style.DARK_TAG)
                Style.forImageView(ivCached, Style.DARK_TAG)
                Style.forProgressBar(progressBar)

                val cached = track.isCached()
                ivDownload.setVisible(!cached)
                ivCached.setVisible(cached)
                progressBar.hide()
                ivButton.setImageDrawable(icon)
                tvTitle.text = track.audio.title
                tvArtist.text = track.audio.artist
                tvDuration.text = secToTime(track.audio.duration)
                setOnClickListener { listener?.invoke(track) }
                ivDownload.setOnClickListener {
                    progressBar.show()
                    ivDownload.hide()
                    onDownload(track)
                }
            }
        }
    }
}
