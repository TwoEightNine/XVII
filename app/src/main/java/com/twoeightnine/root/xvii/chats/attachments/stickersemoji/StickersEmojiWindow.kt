package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Emoji
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.views.KeyboardWindow
import kotlinx.android.synthetic.main.window_stickers.view.*


class StickersEmojiWindow(
        rootView: View,
        context: Context,
        onKeyboardClosed: () -> Unit,
        private val onStickerClicked: (Sticker) -> Unit,
        private val onEmojiClicked: (Emoji) -> Unit
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
        setOnDismissListener {
            repo.destroy()
        }
    }

    private fun loadStickers(forceLoad: Boolean) {
        repo.loadEmojis { emojiPacks ->
            val hasRecentEmoji = emojiPacks.find { it.name == null }?.emojis?.isNotEmpty()
                    ?: false
            repo.loadStickers(forceLoad = forceLoad) { stickerPacks ->
                val hasRecentStickers = stickerPacks.find { it.name == null }?.stickers?.isNotEmpty()
                        ?: false
                with(contentView) {
                    progressBar.hide()
                    val pagerAdapter = PacksPagerAdapter(context, stickerPacks, emojiPacks, WindowCallback())
                    viewPager.adapter = pagerAdapter
                    val posDelta = when {
                        hasRecentStickers -> 0
                        hasRecentEmoji -> -1
                        else -> -2
                    }
                    tabs.setupWithViewPager(viewPager)
                    viewPager.currentItem = pagerAdapter.recentStickersPosition + posDelta

                    for (i in 0 until tabs.tabCount) {
                        val tab = tabs.getTabAt(i)
                        tab?.customView = pagerAdapter.getTabView(i)
                    }
                    tabs.isSmoothScrollingEnabled = true
                    tabs.postDelayed({
                        tabs.getTabAt(viewPager.currentItem)?.select()
                    }, 100L)
                }
            }
        }
    }

    private inner class WindowCallback : PacksPagerAdapter.Callback {

        override fun onStickerClicked(sticker: com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Sticker) {
            this@StickersEmojiWindow.onStickerClicked(Sticker(stickerId = sticker.id))
        }

        override fun onEmojiClicked(emoji: Emoji) {
            this@StickersEmojiWindow.onEmojiClicked(emoji)
            repo.setEmojiUsed(emoji.code)
        }
    }
}