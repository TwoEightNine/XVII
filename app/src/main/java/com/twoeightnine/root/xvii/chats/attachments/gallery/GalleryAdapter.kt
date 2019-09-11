package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseMultiSelectAdapter
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.GalleryItem
import com.twoeightnine.root.xvii.utils.ColorManager
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.item_gallery.view.*

class GalleryAdapter(
        context: Context,
        private val onCameraClick: () -> Unit
) : BaseMultiSelectAdapter<GalleryItem, GalleryAdapter.GalleryViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GalleryViewHolder(inflater.inflate(R.layout.item_gallery, null))

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    companion object {
        val CAMERA_STUB = GalleryItem(0L, "", GalleryItem.Type.PHOTO)
        const val PHOTO_SIZE = 180
    }

    inner class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: GalleryItem) {
            with(itemView) {
                val isCamera = item == CAMERA_STUB
                ivCamera.setVisible(isCamera)
                ivThumb.setVisible(!isCamera)
                if (isCamera) {
                    ivCamera.stylize(ColorManager.MAIN_TAG)
                } else {
                    ivThumb.load("file://${item.path}") {
                        resize(PHOTO_SIZE, PHOTO_SIZE)
                                .centerCrop()
                    }
                }
                ivCheck.setVisible(item in multiSelect)
                ivThumb.setOnClickListener {
                    if (multiSelectMode) {
                        val i = items[adapterPosition]
                        multiSelect(i)
                        ivCheck.setVisible(i in multiSelect)
                    }
                }
                ivCamera.setOnClickListener { onCameraClick() }
            }
        }
    }
}