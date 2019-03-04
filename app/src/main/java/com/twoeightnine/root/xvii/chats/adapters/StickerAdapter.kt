package com.twoeightnine.root.xvii.chats.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.model.Attachment

/**
 * Created by root on 1/10/17.
 */

class StickerAdapter : SimpleAdapter<Attachment.Sticker>() {

    override fun getView(pos: Int, v: View?, viewGroup: ViewGroup): View {
        var view = v
        val sticker = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_sticker, null)
            view!!.tag = StickerViewHolder(view)
        }
        val holder = view.tag as StickerViewHolder
        Picasso.with(App.context)
                .load(sticker.photo256)
                .into(holder.ivSticker)
        return view
    }

    inner class StickerViewHolder(view: View) {

        @BindView(R.id.ivSticker)
        lateinit var ivSticker: ImageView

        init {
            ButterKnife.bind(this, view)
        }
    }
}
