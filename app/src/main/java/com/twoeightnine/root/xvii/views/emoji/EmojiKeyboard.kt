package com.twoeightnine.root.xvii.views.emoji

import android.content.Context
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.views.KeyboardWindow

class EmojiKeyboard(
        rootView: View,
        context: Context,
        private val onEmojiClick: (Emoji) -> Unit,
        onKeyboardClosed: () -> Unit = {}
) : KeyboardWindow(rootView, context, onKeyboardClosed) {

    override fun getAdditionalHeight() = 0

    override fun createView(): View {
        val view = View.inflate(context, R.layout.popup_emoji, null)
        val vpEmoji = view.findViewById<ViewPager>(R.id.viewPager)
//        val tabs = view.findViewById(R.id.tabs) as TabLayout
        val pagerAdapter = EmojiPagerAdapter {
            onEmojiClick.invoke(it)
            val emojis = Prefs.recentEmojis
            if (it in emojis) {
                emojis.remove(it)
            }
            emojis.add(0, it)
            if (emojis.size > 32) {
                emojis.removeAt(emojis.size - 1)
            }
            Prefs.recentEmojis = emojis
        }
        vpEmoji.adapter = pagerAdapter
        vpEmoji.currentItem = 1
        return view
    }

}