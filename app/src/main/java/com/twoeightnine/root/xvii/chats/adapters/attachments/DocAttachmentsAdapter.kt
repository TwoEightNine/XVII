package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Doc
import com.twoeightnine.root.xvii.utils.getSize
import kotlinx.android.synthetic.main.item_attachments_doc.view.*


class DocAttachmentsAdapter(loader: ((Int) -> Unit)?,
                            listener: ((Doc) -> Unit)?) : SimplePaginationAdapter<Doc>(loader, listener) {

    override fun getView(pos: Int, v: View?, viewGroup: ViewGroup): View {
        var view = v
        val doc = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_attachments_doc, null)
            view!!.tag = DocViewHolder(view)
        }
        val holder = view.tag as DocViewHolder
        holder.bind(doc)
        return view
    }

    inner class DocViewHolder(private val view: View) {

        fun bind(doc: Doc) {
            with(view) {
                tvExt.text = doc.ext
                tvTitle.text = doc.title
                tvSize.text = getSize(App.context, doc.size)
                tvTitle.setOnClickListener {
                    listener?.invoke(doc)
                }
                Style.forViewGroup(relativeLayout)
            }
        }
    }
}
