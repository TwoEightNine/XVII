package com.twoeightnine.root.xvii.chats.adapters

import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.utils.loadUrl
import kotlinx.android.synthetic.main.item_sticker.view.*

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
        holder.bind(sticker)
        return view
    }

    inner class StickerViewHolder(private val view: View) {

        fun bind(sticker: Attachment.Sticker) {
            with(view) {
                ivSticker.loadUrl(sticker.photo256)
            }
        }
    }
}
