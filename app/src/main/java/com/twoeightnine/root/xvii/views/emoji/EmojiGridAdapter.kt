package com.twoeightnine.root.xvii.views.emoji

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter

class EmojiGridAdapter: SimpleAdapter<Emoji>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val emoji = items[position]
        var view = convertView
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_emoji, null)
            view.tag = EmojiViewHolder(view)
        }
        val holder = view?.tag as EmojiViewHolder
        Picasso.with(App.context)
                .load("file:///android_asset/emoji/${emoji.res}")
                .into(holder.ivEmoji)
        return view
    }

    inner class EmojiViewHolder(view: View) {

        @BindView(R.id.ivEmoji)
        lateinit var ivEmoji: ImageView

        init {
            ButterKnife.bind(this, view)
        }

    }
}