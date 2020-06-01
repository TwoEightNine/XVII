package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Sticker
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.StickerPack
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_sticker_tab.view.*
import kotlinx.android.synthetic.main.view_sticker_pack.view.*

class StickerPacksPagerAdapter(
        private val context: Context,
        stickersPacks: List<StickerPack>,
        private var onClick: (Sticker) -> Unit
) : PagerAdapter() {

    private val packs = arrayListOf<StickerPack>()
    private val views = arrayListOf<View>()
    private val titles = arrayListOf<String>()

    init {
        stickersPacks.forEach { pack ->
            val view = getView(pack)
            if (pack.name == null) {
                views.add(0, view) // recent
                titles.add(0, context.getString(R.string.recent))
                packs.add(0, pack)
            } else {
                views.add(view)
                titles.add(pack.name.toLowerCase())
                packs.add(pack)
            }
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = views[position]
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, obj: Any) = view == obj

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getPageTitle(position: Int): String = titles[position]

    private fun getPreviewUrl(position: Int): String? = when {
        packs[position].name != null -> packs[position].stickers.getOrNull(0)?.photo128
        else -> null
    }

    fun getTabView(position: Int): View? =
            View.inflate(context, R.layout.item_sticker_tab, null)?.apply {
                val url = getPreviewUrl(position)
                if (url != null) {
                    ivStickerTab.load(url)
                } else {
                    ivStickerTab.setImageResource(R.drawable.ic_clock_recent)
                }
            }

    fun isPageOnTop(position: Int) =
            views[position].rvStickers?.run {
                (adapter as? StickersAdapter)?.firstVisiblePosition(layoutManager) == 0
            }

    override fun getCount() = views.size

    private fun getView(pack: StickerPack): View =
            View.inflate(context, R.layout.view_sticker_pack, null).apply {
                rvStickers.layoutManager =
                        GridLayoutManager(this@StickerPacksPagerAdapter.context, 5)
                rvStickers.adapter = StickersAdapter(context, onClick) {}.apply {
                    addAll(pack.stickers.toMutableList())
                }
            }

}