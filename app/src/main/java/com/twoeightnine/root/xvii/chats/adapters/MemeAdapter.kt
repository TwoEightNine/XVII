package com.twoeightnine.root.xvii.chats.adapters

import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Meme
import com.twoeightnine.root.xvii.utils.loadUrl

class MemeAdapter : SimpleAdapter<Meme>() {

    override fun getView(pos: Int, v: View?, viewGroup: ViewGroup): View {
        var view = v
        val meme = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_sticker, null)
            view!!.tag = StickerViewHolder(view)
        }
        val holder = view.tag as StickerViewHolder
        if (meme.isAddMeme()) {
            val d = ContextCompat.getDrawable(App.context, R.drawable.ic_add)
            Style.forDrawable(d, Style.MAIN_TAG)
            holder.ivSticker.setImageDrawable(d)
        } else {
            Picasso.with(App.context)
                    .loadUrl("file://${meme.path}")
                    .resize(256, 256)
                    .into(holder.ivSticker)
        }
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