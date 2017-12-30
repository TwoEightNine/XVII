package com.twoeightnine.root.xvii.views.emoji

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R

class EmojiFragment: Fragment() {

    @BindView(R.id.gvEmoji)
    lateinit var gvEmoji: GridView

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

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) = View.inflate(activity, R.layout.fragment_emoji, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
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