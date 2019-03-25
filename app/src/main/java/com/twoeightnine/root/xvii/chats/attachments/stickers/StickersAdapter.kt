package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_sticker.view.*

class StickersAdapter(
        context: Context,
        private val onClick: (Attachment.Sticker) -> Unit
) : BaseAdapter<Attachment.Sticker, StickersAdapter.StickerViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = StickerViewHolder(inflater.inflate(R.layout.item_sticker, null))

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class StickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Attachment.Sticker) {
            with(itemView) {
                ivSticker.load(item.photo256, placeholder = false)
                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}