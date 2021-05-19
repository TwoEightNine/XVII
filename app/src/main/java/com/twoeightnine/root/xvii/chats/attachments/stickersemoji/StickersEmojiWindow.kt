/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.views.KeyboardWindow
import global.msnthrp.xvii.data.stickersemoji.model.Emoji
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.isValidForGlide
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
        loadEmojisAndStickers(forceLoad = false)
        setOnDismissListener {
            repo.destroy()
        }
    }

    private fun loadEmojisAndStickers(forceLoad: Boolean) {
        repo.loadEmojis { emojiPacks ->
            val hasRecentEmoji = emojiPacks.find { it.name == null }?.emojis?.isNotEmpty()
                    ?: false
            repo.loadStickers(forceLoad = forceLoad) { stickerPacks ->
                if (!context.isValidForGlide()) return@loadStickers

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

        override fun onStickerClicked(sticker: global.msnthrp.xvii.data.stickersemoji.model.Sticker) {
            this@StickersEmojiWindow.onStickerClicked(Sticker(stickerId = sticker.id))
        }

        override fun onEmojiClicked(emoji: Emoji) {
            this@StickersEmojiWindow.onEmojiClicked(emoji)
            repo.setEmojiUsed(emoji.code)
        }
    }
}