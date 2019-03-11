package com.twoeightnine.root.xvii.views.emoji

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.fragment_emoji.*

class EmojiFragment: androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance(listener: (Emoji) -> Unit, emojis: MutableList<Emoji>): EmojiFragment {
            val frag = EmojiFragment()
            frag.listener = listener
            frag.emojis.addAll(emojis)
            return frag
        }
    }

    private lateinit var adapter: EmojiGridAdapter

    private lateinit var listener: (Emoji) -> Unit
    private val emojis = mutableListOf<Emoji>()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) = View.inflate(activity, R.layout.fragment_emoji, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = EmojiGridAdapter()
        adapter.add(emojis)
        gvEmoji.adapter = adapter
        gvEmoji.setOnItemClickListener {
            _, _, position, _ ->
            val emoji = adapter.items[position]
            listener.invoke(emoji)
        }
    }
}