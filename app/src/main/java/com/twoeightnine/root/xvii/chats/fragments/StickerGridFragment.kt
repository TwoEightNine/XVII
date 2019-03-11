package com.twoeightnine.root.xvii.chats.fragments

import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.StickerAdapter
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.StickerPack
import kotlinx.android.synthetic.main.fragment_sticker_grid.*

/**
 * Created by root on 3/7/17.
 */

class StickerGridFragment : BaseFragment() {

    companion object {

        const val RECENT_COUNT = 50

        fun newInstance(pack: StickerPack, listener: ((Attachment.Sticker) -> Unit)?): StickerGridFragment {
            val frag = StickerGridFragment()
            frag.pack = pack
            frag.listener = listener
            return frag
        }
    }

    lateinit var pack: StickerPack
    private lateinit var adapter: StickerAdapter

    var listener: ((Attachment.Sticker) -> Unit)? = null

    private lateinit var recent: MutableList<Int>
    private lateinit var avail: MutableList<Int>

    override fun bindViews(view: View) {
        super.bindViews(view)
        adapter = StickerAdapter()
        recent = Prefs.recentStickers
        avail = Prefs.availableStickers
        if (pack.isRecent) {
            recent.forEach { adapter.add(Attachment.Sticker(it)) }
        } else if (pack.isAvailable) {
            Prefs.availableStickers.forEach { adapter.add(Attachment.Sticker(it)) }
        } else {
            for (i in 0 until pack.count) {
                adapter.add(Attachment.Sticker(pack.getSticker(i)))
            }
        }


        gvStickers.adapter = adapter
        gvStickers.setOnItemClickListener {
            _, _, pos, _ ->
            val sticker = adapter.items[pos]
            if (sticker.id in recent) {
                recent.remove(sticker.id)
            }
            recent.add(0, sticker.id)
            if (recent.size > RECENT_COUNT) {
                recent.removeAt(recent.size - 1)
            }
            Prefs.recentStickers = recent
            listener?.invoke(sticker)
        }
    }

    override fun getLayout() = R.layout.fragment_sticker_grid


}
