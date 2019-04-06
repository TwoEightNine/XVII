package com.twoeightnine.root.xvii.views.emoji

import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_emoji.view.*

class EmojiGridAdapter : SimpleAdapter<Emoji>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val emoji = items[position]
        var view = convertView
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_emoji, null)
            view.tag = EmojiViewHolder(view)
        }
        val holder = view?.tag as EmojiViewHolder
        holder.bind(emoji)
        return view
    }

    inner class EmojiViewHolder(private val view: View) {

        fun bind(emoji: Emoji) {
            with(view) {
                ivKeyboard.load("file:///android_asset/emoji/${emoji.res}", placeholder = false)
            }
        }

    }
}