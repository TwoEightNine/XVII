package com.twoeightnine.root.xvii.chats.adapters

import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimplePaginationAdapter
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.setVisible
import kotlinx.android.synthetic.main.item_gallery.view.*

class GalleryAdapter(loader: ((Int) -> Unit)?,
                     listener: ((String) -> Unit)?) : SimplePaginationAdapter<String>(loader, listener) {

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

        const val THUMB_WIDTH = 300
    }

    inner class GalleryViewHolder(private val view: View) {

        fun bind(path: String) {
            with(view) {
                ivThumb.setImageResource(R.drawable.placeholder)
                if (path == CAMERA_MARKER) {
                    ivThumb.setImageResource(R.drawable.layer_camera)
                } else {
                    ivThumb.load("file://$path") {
                        resize(THUMB_WIDTH, THUMB_WIDTH)
                                .centerCrop()
                    }
                }
                ivCheck.setVisible(path in multiSelectRaw)
            }
        }

    }
}
