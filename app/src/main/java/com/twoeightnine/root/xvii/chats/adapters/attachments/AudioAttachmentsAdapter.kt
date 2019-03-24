package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.background.MediaPlayerAsyncTask
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Audio
import com.twoeightnine.root.xvii.utils.showError
import kotlinx.android.synthetic.main.item_attachments_audio.view.*


class AudioAttachmentsAdapter(loader: ((Int) -> Unit)?,
                              listener: ((Audio) -> Unit)?) : SimplePaginationAdapter<Audio>(loader, listener) {

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
                val dPlay = ContextCompat.getDrawable(context, R.drawable.play_big)
                val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)
                Style.forDrawable(dPlay, Style.DARK_TAG)
                Style.forDrawable(dPause, Style.DARK_TAG)
                ivButton.setImageDrawable(dPlay)
                tvTitle.text = audio.title
                tvArtist.text = audio.artist
                if (Prefs.playerUrl == audio.url && RootActivity.player != null) {
                    ivButton.setImageDrawable(dPause)
                }
                ivButton.setOnClickListener {
                    if (RootActivity.player != null && RootActivity.player!!.isExecuting) {
                        RootActivity.player!!.cancel(true)
                        RootActivity.player = null
                        ivButton.setImageDrawable(dPlay)
                    } else {
                        if (RootActivity.player == null) {
                            RootActivity.player = MediaPlayerAsyncTask {
                                ivButton.setImageDrawable(dPlay)
                                RootActivity.player = null
                            }
                        }
                        if (!RootActivity.player!!.isExecuting) {
                            if (!audio.url.isNullOrEmpty()) {
                                try {
                                    RootActivity.player!!.execute(audio.url)
                                    ivButton.setImageDrawable(dPause)
                                } catch (e: IllegalStateException) {
                                    Lg.i("container player: ${e.message}")
                                    RootActivity.player!!.cancel(true)
                                }
                            } else {
                                showError(context, R.string.audio_denied)
                            }
                        }
                    }
                }
            }
        }
    }
}
