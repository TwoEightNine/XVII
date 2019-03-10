package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.utils.loadUrl
import kotlinx.android.synthetic.main.item_photo_attachment.view.*

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
        holder.bind(photo)
        return view
    }


    inner class PhotoViewHolder(private val view: View) {

        fun bind(photo: Photo) {
            with(view) {
                ivPhoto.loadUrl(photo.optimalPhoto)
                ivPhoto.setOnClickListener {
                    listener?.invoke(photo)
                }
                if (photo in multiSelectRaw) {
                    ivCheck.visibility = View.VISIBLE
                } else {
                    ivCheck.visibility = View.GONE
                }
            }
        }
    }

}
