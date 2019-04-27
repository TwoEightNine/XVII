package com.twoeightnine.root.xvii.chats.attachments.docs

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsAdapter
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.utils.getSize
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.item_attachments_doc.view.*

class DocAttachmentsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Doc) -> Unit
) : BaseAttachmentsAdapter<Doc, DocAttachmentsAdapter.DocViewHolder>(context, loader) {

    override fun getViewHolder(view: View) = DocViewHolder(view)

    override fun getLayoutId() = R.layout.item_attachments_doc

    override fun createStubLoadItem() = Doc()

    inner class DocViewHolder(view: View) : BaseAttachmentViewHolder<Doc>(view) {

        override fun bind(item: Doc) {
            with(itemView) {
                tvExt.text = item.ext
                tvTitle.text = item.title
                tvSize.text = getSize(App.context, item.size)
                relativeLayout.stylize(changeStroke = false)
                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}