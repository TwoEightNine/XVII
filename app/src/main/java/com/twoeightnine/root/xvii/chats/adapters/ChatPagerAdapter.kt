package com.twoeightnine.root.xvii.chats.adapters

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.twoeightnine.root.xvii.chats.fragments.ChatAttachFragment
import com.twoeightnine.root.xvii.chats.fragments.ChatSendFragment

class ChatPagerAdapter(fragmentManager: androidx.fragment.app.FragmentManager,
                       sendListener: (String) -> Unit,
                       longSendListener: (String) -> Boolean,
                       emojiListener: () -> Unit,
                       typingListener: () -> Unit,
                       attachListener: (Int) -> Unit,
                       viewAttachmentsListener: () -> Unit): androidx.fragment.app.FragmentStatePagerAdapter(fragmentManager) {

    var attachFrag = ChatAttachFragment.newInstance(attachListener)
    var sendFrag = ChatSendFragment.newInstance(sendListener, longSendListener, typingListener, emojiListener, viewAttachmentsListener)

    override fun getItem(position: Int) =
        when(position) {
            0 -> attachFrag
            else -> sendFrag
        }

    override fun getCount() = 2
}