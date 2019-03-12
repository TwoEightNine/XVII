package com.twoeightnine.root.xvii.chats.adapters

import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import kotlinx.android.synthetic.main.item_gallery.view.*

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
        holder.bind(path)
        return view
    }

    companion object {
        const val CAMERA_MARKER = "cameraMarker"
    }

    inner class GalleryViewHolder(private val view: View) {

        fun bind(path: String) {
            with(view) {
                ivThumb.setImageResource(R.drawable.placeholder)
                if (path == CAMERA_MARKER) {
                    ivThumb.setImageResource(R.drawable.layer_camera)
                } else {
                    Picasso.get()
                            .load("file://$path")
                            .resize(300, 300)
                            .centerCrop()
                            .into(ivThumb)
                }
                if (path in multiSelectRaw) {
                    ivCheck.visibility = View.VISIBLE
                } else {
                    ivCheck.visibility = View.GONE
                }
            }
        }

    }
}
