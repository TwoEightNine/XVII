package com.twoeightnine.root.xvii.chats.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter

class GalleryAdapter(loader: ((Int) -> Unit)?,
                     listener: ((String) -> Unit)?): SimplePaginationAdapter<String>(loader, listener) {

    override fun getView(pos: Int, v: View?, p2: ViewGroup?): View {
        var view = v
        val path = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_gallery, null)
            view.tag = GalleryViewHolder(view)
        }
        val holder = view?.tag as GalleryViewHolder
        holder.ivThumb.setImageResource(R.drawable.placeholder)
        Glide.with(App.context)
                .load("file://$path")
                .into(holder.ivThumb)
        if (path in multiSelectRaw) {
            holder.ivCheck.visibility = View.VISIBLE
        } else {
            holder.ivCheck.visibility = View.GONE
        }
        return view
    }

    inner class GalleryViewHolder(view: View) {
        @BindView(R.id.ivThumb)
        lateinit var ivThumb: ImageView
        @BindView(R.id.ivCheck)
        lateinit var ivCheck: ImageView

        init {
            ButterKnife.bind(this, view)
        }

    }
}
