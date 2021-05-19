/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.chats.attachments.audios

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsAdapter
import com.twoeightnine.root.xvii.model.attachments.Audio
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.secToTime
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.setVisible
import global.msnthrp.xvii.uikit.extensions.show
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
                    dPause?.paint(Munch.color.color)
                    dPause
                } else {
                    val dPlay = ContextCompat.getDrawable(context, R.drawable.ic_play)
                    dPlay?.paint(Munch.color.color)
                    dPlay
                }
                ivDownload.paint(Munch.color.color)
                ivCached.paint(Munch.color.color)

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