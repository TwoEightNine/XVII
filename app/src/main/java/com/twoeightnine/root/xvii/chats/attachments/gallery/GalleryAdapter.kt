package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsAdapter
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.setVisible
import kotlinx.android.synthetic.main.item_photo_attachment.view.*

class GalleryAdapter(
        context: Context,
        private val onCameraClick: () -> Unit,
        loader: (Int) -> Unit = {}
) : BaseAttachmentsAdapter<String, GalleryAdapter.GalleryViewHolder>(context, loader) {

    override fun getLayoutId() = R.layout.item_photo_attachment

    override fun getViewHolder(view: View) = GalleryViewHolder(view)

    override fun createStubLoadItem() = "stub"

    companion object {
        const val CAMERA_STUB = "cameraaa"
        const val PHOTO_SIZE = 300
    }

    inner class GalleryViewHolder(view: View) : BaseAttachmentViewHolder<String>(view) {

        override fun bind(item: String) {
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