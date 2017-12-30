package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.utils.loadUrl

class PhotoAttachmentsAdapter(var context: Context,
                              loader: ((Int) -> Unit)?,
                              listener: ((Photo) -> Unit)?) : SimplePaginationAdapter<Photo>(loader, listener) {

    override fun getView(pos: Int, v: View?, viewGroup: ViewGroup): View {
        var view = v
        val photo = items[pos]
        if (view == null) {
            view = View.inflate(context, R.layout.item_photo_attachment, null)
            view!!.tag = PhotoViewHolder(view)
        }
        val holder = view.tag as PhotoViewHolder
        holder.ivPhoto.loadUrl(photo.optimalPhoto)
        holder.ivPhoto.setOnClickListener {
            listener?.invoke(photo)
        }
        if (photo in multiSelectRaw) {
            holder.ivCheck.visibility = View.VISIBLE
        } else {
            holder.ivCheck.visibility = View.GONE
        }
        return view
    }


    inner class PhotoViewHolder(view: View) {

        @BindView(R.id.ivPhoto)
        lateinit var ivPhoto: ImageView
        @BindView(R.id.ivCheck)
        lateinit var ivCheck: ImageView

        init {
            ButterKnife.bind(this, view)
        }
    }

}
