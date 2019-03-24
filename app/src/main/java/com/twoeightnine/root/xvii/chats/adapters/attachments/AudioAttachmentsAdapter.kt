package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Audio
import com.twoeightnine.root.xvii.utils.secToTime
import kotlinx.android.synthetic.main.item_attachments_audio.view.*


class AudioAttachmentsAdapter(loader: ((Int) -> Unit)?,
                              listener: ((Audio) -> Unit)?) : SimplePaginationAdapter<Audio>(loader, listener) {

    var played: Audio? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getView(pos: Int, v: View?, viewGroup: ViewGroup): View {
        var view = v
        val doc = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_attachments_audio, null)
            view!!.tag = AudioViewHolder(view)
        }
        val holder = view.tag as AudioViewHolder
        holder.bind(doc)
        return view
    }

    inner class AudioViewHolder(private val view: View) {

        fun bind(audio: Audio) {
            with(view) {
                val icon = if (audio == played) {
                    val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)
                    Style.forDrawable(dPause, Style.DARK_TAG)
                    dPause
                } else {
                    val dPlay = ContextCompat.getDrawable(context, R.drawable.play_big)
                    Style.forDrawable(dPlay, Style.DARK_TAG)
                    dPlay
                }
                ivButton.setImageDrawable(icon)
                tvTitle.text = audio.title
                tvArtist.text = audio.artist
                tvDuration.text = secToTime(audio.duration)
                setOnClickListener { listener?.invoke(audio) }
            }
        }
    }
}
