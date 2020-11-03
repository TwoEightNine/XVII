package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseReachAdapter
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.DeviceItem
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.secToTime
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_gallery.view.*

class GalleryAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (DeviceItem) -> Unit
) : BaseReachAdapter<DeviceItem, GalleryAdapter.GalleryViewHolder>(context, loader) {

    fun checkSelected(path: String) {
        items.find { it.path == path }?.also { deviceItem ->
            if (deviceItem !in multiSelect) {
                multiSelect(deviceItem)
                notifyItemChanged(items.indexOf(deviceItem))
            }
        }
    }

    override fun createHolder(parent: ViewGroup, viewType: Int) =
            GalleryViewHolder(inflater.inflate(R.layout.item_gallery, null))

    override fun bind(holder: GalleryViewHolder, item: DeviceItem) {
        holder.bind(item)
    }

    override fun createStubLoadItem() = DeviceItem(0, "", DeviceItem.Type.VIDEO, 23)

    override fun getThreshold() = THRESHOLD

    companion object {
        const val THRESHOLD = 20
        const val SIZE_PX = 200

        const val ANIM_DURATION = 100L
        const val SCALE_THUMB_SELECTED = .95f
        const val SCALE_THUMB_DEFAULT = 1f
        const val SCALE_CHECK_SELECTED = 1f
        const val SCALE_CHECK_DEFAULT = 0f
    }

    inner class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: DeviceItem) {
            with(itemView) {
                tvDuration.setVisible(item.type == DeviceItem.Type.VIDEO)
                tvDuration.text = secToTime((item.duration / 1000L).toInt())
                if (item.thumbnail != null) {
                    ivThumb.load(item.thumbnail) {
                        resize(SIZE_PX, SIZE_PX)
                        centerCrop()
                    }
                }
                invalidateCheck(item, animate = false)
                setOnClickListener {
                    val deviceItem = items[adapterPosition]
                    if (deviceItem.type == DeviceItem.Type.PHOTO && item !in multiSelect) {
                        onClick(deviceItem)
                    } else {
                        rlCheck.callOnClick()
                    }
                }
                rlCheck.setOnClickListener {
                    if (multiSelectMode) {
                        val i = items[adapterPosition]
                        multiSelect(i)
                        invalidateCheck(i)
                    }
                }
                ivCheckCircle.paint(Munch.color.color)
            }
        }

        private fun invalidateCheck(item: DeviceItem, animate: Boolean = true) {
            with(itemView) {
                val selected = item in multiSelect
                val scaleThumb = if (selected) SCALE_THUMB_SELECTED else SCALE_THUMB_DEFAULT
                val scaleCheck = if (selected) SCALE_CHECK_SELECTED else SCALE_CHECK_DEFAULT
                val duration = if (animate) ANIM_DURATION else 0L
                ivThumb.animate()
                        .scaleX(scaleThumb)
                        .scaleY(scaleThumb)
                        .setDuration(duration)
                        .start()
                rlCheck.animate()
                        .scaleX(scaleCheck)
                        .scaleY(scaleCheck)
                        .setDuration(duration)
                        .start()
            }
        }
    }
}