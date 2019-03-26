package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.setVisible
import kotlinx.android.synthetic.main.item_photo_attachment.view.*

class GalleryAdapter(
        context: Context,
        private val onCameraClick: () -> Unit
) : BaseAdapter<String, GalleryAdapter.GalleryViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GalleryViewHolder(inflater.inflate(R.layout.item_photo_attachment, null))

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    companion object {
        const val CAMERA_STUB = "cameraaa"
        const val PHOTO_SIZE = 300
    }

    inner class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: String) {
            with(itemView) {
                if (item == CAMERA_STUB) {
                    ivPhoto.setImageResource(R.drawable.layer_camera)
                } else {
                    ivPhoto.load("file://$item") {
                        resize(GalleryAdapter.PHOTO_SIZE, GalleryAdapter.PHOTO_SIZE)
                                .centerCrop()
                    }
                }
                ivCheck.setVisible(item in multiSelect)
                ivPhoto.setOnClickListener {
                    when {
                        item == CAMERA_STUB -> onCameraClick()
                        multiSelectMode -> {
                            val i = items[adapterPosition]
                            multiSelect(i)
                            ivCheck.setVisible(i in multiSelect)
                        }
                    }
                }
            }
        }
    }
}