package com.twoeightnine.root.xvii.chats.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.model.StickerPack
import com.twoeightnine.root.xvii.utils.loadUrl
import kotlinx.android.synthetic.main.item_sticker_cat.view.*

/**
 * Created by your mama on 7/11/17.
 */
class StickerCatAdapter(context: Context,
                        var listener: ((StickerPack) -> Unit)?) : BaseAdapter<StickerPack, StickerCatAdapter.StickerCatViewHolder>(context) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            StickerCatViewHolder(View.inflate(context, R.layout.item_sticker_cat, null))


    override fun onBindViewHolder(holder: StickerCatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class StickerCatViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun bind(pack: StickerPack) {
            with(itemView) {
                when {
                    pack.isRecent -> ivStickerItem.setImageResource(R.drawable.ic_recent)
                    pack.isAvailable -> ivStickerItem.setImageResource(R.drawable.ic_feed)
                    else -> {
                        ivStickerItem.loadUrl(pack.getStickerUrl(0))
                    }
                }
            }
        }
    }
}