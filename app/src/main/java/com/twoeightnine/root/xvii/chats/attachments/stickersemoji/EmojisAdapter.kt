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

package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.managers.Prefs
import global.msnthrp.xvii.data.stickersemoji.model.Emoji
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.isValidForGlide
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_emoji.view.*

class EmojisAdapter(
        context: Context,
        private val onClick: (Emoji) -> Unit,
        private val onLongClick: (Emoji) -> Unit
) : BaseAdapter<Emoji, EmojisAdapter.EmojiViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            EmojiViewHolder(inflater.inflate(R.layout.item_emoji, null))

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class EmojiViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(emoji: Emoji) {
            if (!itemView.context.isValidForGlide()) return

            with(itemView) {

                ivKeyboard.setVisible(Prefs.appleEmojis)
                tvEmoji.setVisible(!Prefs.appleEmojis)

                if (Prefs.appleEmojis) {
                    ivKeyboard.load(emoji.fullPath, placeholder = false)
                } else {
                    tvEmoji.text = emoji.code
                }

                setOnClickListener { onClick(items[adapterPosition]) }
                setOnLongClickListener { onLongClick(items[adapterPosition]); true }
            }
        }
    }
}