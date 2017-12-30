package com.twoeightnine.root.xvii.chats.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.model.StickerPack

/**
 * Created by your mama on 7/11/17.
 */
class StickerCatAdapter(context: Context,
                        var listener: ((StickerPack) -> Unit)?) : BaseAdapter<StickerPack, StickerCatAdapter.StickerCatViewHolder>(context) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            StickerCatViewHolder(View.inflate(context, R.layout.item_sticker_cat, null))


    override fun onBindViewHolder(holder: StickerCatViewHolder, position: Int) {
        val pack = items[position]
        if (pack.isRecent) {
            holder.ivItem.setImageResource(R.drawable.ic_recent)
        } else if (pack.isAvailable) {
            holder.ivItem.setImageResource(R.drawable.ic_feed)
        } else {
            Picasso.with(context)
                    .load(pack.getStickerUrl(0))
                    .into(holder.ivItem)
        }
    }

    inner class StickerCatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @BindView(R.id.ivStickerItem)
        lateinit var ivItem: ImageView

        init {
            ButterKnife.bind(this, view)
            ivItem.setOnClickListener {
                listener?.invoke(items[adapterPosition])
            }
        }
    }
}