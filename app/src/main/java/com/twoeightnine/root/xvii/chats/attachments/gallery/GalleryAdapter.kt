package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.DeviceItem
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.item_gallery.view.*

class GalleryAdapter(
        context: Context,
        private val onCameraClick: () -> Unit,
        loader: (Int) -> Unit
) : BaseReachAdapter<DeviceItem, GalleryAdapter.GalleryViewHolder>(context, loader) {

    override fun createHolder(parent: ViewGroup, viewType: Int) =
            GalleryViewHolder(inflater.inflate(R.layout.item_gallery, null))

    override fun bind(holder: GalleryViewHolder, item: DeviceItem) {
        holder.bind(item)
    }

    override fun createStubLoadItem() = DeviceItem(0, "", DeviceItem.Type.VIDEO, 23)

    override fun getThreshold() = THRESHOLD

    companion object {
        const val THRESHOLD = 20
        val CAMERA_STUB = DeviceItem(0L, "", DeviceItem.Type.PHOTO)
    }

    inner class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: DeviceItem) {
            with(itemView) {
                val isCamera = item == CAMERA_STUB
                ivCamera.setVisible(isCamera)
                ivThumb.setVisible(!isCamera)
                tvDuration.setVisible(item.type == DeviceItem.Type.VIDEO)
                tvDuration.text = secToTime((item.duration / 1000L).toInt())
                when {
                    isCamera -> ivCamera.stylize(ColorManager.MAIN_TAG)
                    item.thumbnail != null -> ivThumb.load(item.thumbnail)
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