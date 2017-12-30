package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Doc
import com.twoeightnine.root.xvii.utils.getSize


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
        holder.tvExt.text = doc.ext
        holder.tvTitle.text = doc.title
        holder.tvSize.text = getSize(App.context, doc.size)
        holder.tvTitle.setOnClickListener {
            listener?.invoke(doc)
        }
        Style.forViewGroup(holder.rlShape)
        return view
    }

    inner class DocViewHolder(view: View) {

        @BindView(R.id.relativeLayout)
        lateinit var rlShape: RelativeLayout
        @BindView(R.id.tvExt)
        lateinit var tvExt: TextView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvSize)
        lateinit var tvSize: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }
}
