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