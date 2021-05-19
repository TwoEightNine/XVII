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

package com.twoeightnine.root.xvii.chats.attachments.stickers.preview

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import kotlinx.android.synthetic.main.item_sticker_keyword.view.*

class StickerKeywordsAdapter(
        context: Context
) : BaseAdapter<String, StickerKeywordsAdapter.KeywordViewHolder>(context) {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int) = KeywordViewHolder(inflater.inflate(R.layout.item_sticker_keyword, parent, false))

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class KeywordViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(keyword: String) {
            with(itemView) {
                tvKeyword.text = keyword

                ivRemove.setOnClickListener {
                    removeAt(adapterPosition)
                }
            }
        }
    }
}