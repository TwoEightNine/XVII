package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.Link


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
        holder.tvTitle.text = link.title
        holder.tvCaption.text = link.caption
        if (link.photo != null) {
            Picasso.with(App.context)
                    .load(link.photo.optimalPhoto)
                    .into(holder.ivPhoto)
        } else {
            holder.ivPhoto.setImageDrawable(null)
        }
        holder.tvTitle.setOnClickListener({ listener?.invoke(link) })
        holder.tvCaption.setOnClickListener({ listener?.invoke(link) })
        return view
    }

    inner class LinkViewHolder(view: View) {
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvCaption)
        lateinit var tvCaption: TextView
        @BindView(R.id.ivPhoto)
        lateinit var ivPhoto: ImageView

        init {
            ButterKnife.bind(this, view)
        }
    }
}
