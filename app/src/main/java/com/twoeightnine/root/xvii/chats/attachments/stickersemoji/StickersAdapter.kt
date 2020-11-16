package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Sticker
import com.twoeightnine.root.xvii.extensions.load
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import kotlinx.android.synthetic.main.item_sticker.view.*

class StickersAdapter(
        context: Context,
        private val onClick: (Sticker) -> Unit,
        private val onLongClick: (Sticker) -> Unit
) : BaseAdapter<Sticker, StickersAdapter.StickerViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            StickerViewHolder(inflater.inflate(R.layout.item_sticker, null))

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class StickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Sticker) {
            with(itemView) {
                ivSticker.load(item.photo256, placeholder = false)
                setOnClickListener { onClick(items[adapterPosition]) }
                setOnLongClickListener { onLongClick(items[adapterPosition]); true }
            }
        }
    }
}