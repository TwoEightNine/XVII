package com.twoeightnine.root.xvii.chats.messages.chat.secret

import android.os.Bundle
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesFragment
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatMessagesFragment
import com.twoeightnine.root.xvii.dialogs.models.Dialog

class SecretChatMessagesFragment : BaseChatMessagesFragment<SecretChatViewModel>() {

    override fun getViewModelClass() = SecretChatViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    companion object {

        fun newInstance(dialog: Dialog): ChatMessagesFragment {
            val fragment = ChatMessagesFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, dialog.peerId)
                putString(ARG_TITLE, dialog.alias ?: dialog.title)
                putString(ARG_PHOTO, dialog.photo)
            }
            return fragment
        }
    }
}