package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsAdapter
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_sticker.view.*

class StickersAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Attachment.Sticker) -> Unit
) : BaseAttachmentsAdapter<Attachment.Sticker, StickersAdapter.StickerViewHolder>(context, loader) {

    override fun getViewHolder(view: View) = StickerViewHolder(view)

    override fun getLayoutId() = R.layout.item_sticker

    override fun createStubLoadItem() = Attachment.Sticker(-3)

    inner class StickerViewHolder(view: View)
        : BaseAttachmentsAdapter.BaseAttachmentViewHolder<Attachment.Sticker>(view) {

        override fun bind(item: Attachment.Sticker) {
            with(itemView) {
                ivSticker.load(item.photo256, placeholder = false)
                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}