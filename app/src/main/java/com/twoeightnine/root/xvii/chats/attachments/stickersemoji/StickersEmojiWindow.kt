package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.utils.stylize
import com.twoeightnine.root.xvii.views.KeyboardWindow
import kotlinx.android.synthetic.main.window_stickers.view.*


class StickersEmojiWindow(
        rootView: View,
        context: Context,
        onKeyboardClosed: () -> Unit,
        private val onStickerClicked: (Sticker) -> Unit
) : KeyboardWindow(rootView, context, onKeyboardClosed) {

    private val repo by lazy {
        StickersEmojiRepository()
    }

    override fun getAdditionalHeight() = 0

    override fun createView(): View =
            View.inflate(context, R.layout.window_stickers, null)

    override fun onViewCreated() {
        super.onViewCreated()
        loadStickers(forceLoad = false)
        contentView.progressBar.stylize()
        setOnDismissListener {
            repo.destroy()
        }
    }

    private fun loadStickers(forceLoad: Boolean) {
        repo.loadStickers(forceLoad = forceLoad) { packs ->
            val hasRecent = packs.find { it.name == null }?.stickers?.isNotEmpty() ?: false
            with(contentView) {
                progressBar.hide()
                val pagerAdapter = StickerPacksPagerAdapter(context, packs) { sticker ->
                    repo.setStickerUsed(sticker)
                    onStickerClicked(Sticker(stickerId = sticker.id))
                }
                viewPager.adapter = pagerAdapter
                viewPager.currentItem = if (hasRecent) 0 else 1
                tabs.setupWithViewPager(viewPager)

                for (i in 0 until tabs.tabCount) {
                    val tab = tabs.getTabAt(i)
                    tab?.customView = pagerAdapter.getTabView(i)
                }
            }
        }
    }
}