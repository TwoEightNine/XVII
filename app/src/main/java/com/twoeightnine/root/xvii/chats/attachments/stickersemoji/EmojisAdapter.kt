package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Emoji
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.load
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_emoji.view.*

class EmojisAdapter(
        context: Context,
        private val onClick: (Emoji) -> Unit,
        private val onLongClick: (Emoji) -> Unit
) : BaseAdapter<Emoji, EmojisAdapter.EmojiViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            EmojiViewHolder(inflater.inflate(R.layout.item_emoji, null))

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class EmojiViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(emoji: Emoji) {
            with(itemView) {

                ivKeyboard.setVisible(Prefs.appleEmojis)
                tvEmoji.setVisible(!Prefs.appleEmojis)

                if (Prefs.appleEmojis) {
                    ivKeyboard.load(emoji.fullPath, placeholder = false)
                } else {
                    tvEmoji.text = emoji.code
                }

                setOnClickListener { onClick(items[adapterPosition]) }
                setOnLongClickListener { onLongClick(items[adapterPosition]); true }
            }
        }
    }
}