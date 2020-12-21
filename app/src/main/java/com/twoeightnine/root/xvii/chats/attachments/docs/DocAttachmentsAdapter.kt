package com.twoeightnine.root.xvii.chats.attachments.docs

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsAdapter
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.utils.getSize
import global.msnthrp.xvii.uikit.extensions.setVisible
import global.msnthrp.xvii.uikit.utils.color.DocColors
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
                val extSafe = item.ext ?: ""
                tvExt.text = prettifyExt(extSafe)
                tvTitle.text = item.title
                tvSize.text = getSize(resources, item.size)

                val preview = item.preview?.photo?.getSmallPreview()?.src
                val hasPreview = preview != null

                ivDocPreview.setVisible(hasPreview)
                tvExt.setVisible(!hasPreview)

                if (preview != null) {
                    ivDocPreview.load(preview)
                } else {
                    ivDocPreview.setImageDrawable(null)
                }
                cvDocPreview.setCardBackgroundColor(
                        DocColors.getColorByExtension(extSafe) ?: Munch.color.color
                )
                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }

        private fun prettifyExt(ext: String): String = when {
            ext.length <= 4 -> ext
            else -> {
                val cropped = ext.take(8)
                val center = cropped.length / 2
                "${cropped.substring(0, center)}\n${cropped.substring(center)}"
            }
        }
    }
}