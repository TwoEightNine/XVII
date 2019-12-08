package com.twoeightnine.root.xvii.chats.messages.chat

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_sticker.view.*

class StickersSuggestionAdapter(
        context: Context,
        private val onClick: (Sticker) -> Unit
) : BaseAdapter<Sticker, StickersSuggestionAdapter.StickerViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            StickerViewHolder(inflater.inflate(R.layout.item_sticker_suggestion, parent, false))

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class StickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(sticker: Sticker) {
            with(itemView) {
                ivSticker.load(sticker.photo256, placeholder = false)

                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}