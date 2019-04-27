package com.twoeightnine.root.xvii.chats.attachments.audios

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsAdapter
import com.twoeightnine.root.xvii.model.attachments.Audio
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.item_attachments_track.view.*

class AudioAttachmentsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Track) -> Unit,
        private val onLongClick: (Track) -> Unit,
        private val onDownload: (Track) -> Unit,
        private val cacheMode: Boolean = false

) : BaseAttachmentsAdapter<Track, AudioAttachmentsAdapter.AudioViewHolder>(context, loader) {

    var played: Track? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getViewHolder(view: View) = AudioViewHolder(view)

    override fun getLayoutId() = R.layout.item_attachments_track

    override fun createStubLoadItem() = Track(Audio())

    inner class AudioViewHolder(view: View)
        : BaseAttachmentViewHolder<Track>(view) {

        override fun bind(item: Track) {
            with(itemView) {
                val icon = if (item == played) {
                    val dPause = ContextCompat.getDrawable(context, R.drawable.ic_pause)
                    dPause?.stylize(ColorManager.DARK_TAG)
                    dPause
                } else {
                    val dPlay = ContextCompat.getDrawable(context, R.drawable.ic_play)
                    dPlay?.stylize(ColorManager.DARK_TAG)
                    dPlay
                }
                ivDownload.stylize(ColorManager.DARK_TAG)
                ivCached.stylize(ColorManager.DARK_TAG)
                progressBar.stylize()

                val cached = item.isCached()
                ivDownload.setVisible(!cached && cacheMode)
                ivCached.setVisible(cached && cacheMode)
                progressBar.hide()
                ivButton.setImageDrawable(icon)
                tvTitle.text = item.audio.title
                tvArtist.text = item.audio.artist
                tvDuration.text = secToTime(item.audio.duration)
                setOnClickListener { onClick(items[adapterPosition]) }
                setOnLongClickListener {
                    onLongClick(items[adapterPosition])
                    true
                }
                ivDownload.setOnClickListener {
                    progressBar.show()
                    ivDownload.hide()
                    onDownload(items[adapterPosition])
                }
            }
        }
    }
}