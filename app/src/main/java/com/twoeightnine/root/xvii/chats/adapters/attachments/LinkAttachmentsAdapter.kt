package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.Link
import kotlinx.android.synthetic.main.item_attachments_link.view.*


class LinkAttachmentsAdapter(loader: ((Int) -> Unit)?,
                             listener: ((Link) -> Unit)?) : SimplePaginationAdapter<Link>(loader, listener) {

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val link = items[i]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_attachments_link, null)
            view!!.tag = LinkViewHolder(view)
        }
        val holder = view.tag as LinkViewHolder
        holder.bind(link)
        return view
    }

    inner class LinkViewHolder(private val view: View) {

        fun bind(link: Link) {
            with(view) {
                tvTitle.text = link.title
                tvCaption.text = link.caption
                if (link.photo != null) {
                    Picasso.get()
                            .load(link.photo.optimalPhoto)
                            .into(ivPhoto)
                } else {
                    ivPhoto.setImageDrawable(null)
                }
                tvTitle.setOnClickListener { listener?.invoke(link) }
                tvCaption.setOnClickListener { listener?.invoke(link) }
            }
        }
    }
}
