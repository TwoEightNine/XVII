package com.twoeightnine.root.xvii.chatowner.fragments

import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.Conversation
import com.twoeightnine.root.xvii.utils.setVisible
import kotlinx.android.synthetic.main.fragment_chat_owner_conversation.*

class ConversationChatOwnerFragment : BaseChatOwnerFragment<Conversation>() {

    override fun getLayoutId() = R.layout.fragment_chat_owner_conversation

    override fun getChatOwnerClass() = Conversation::class.java

    override fun bindChatOwner(chatOwner: Conversation?) {
        val conversation = chatOwner ?: return

        fabOpenChat.setVisible(conversation.canWrite?.allowed != false)
        addValue(R.drawable.ic_pinned, conversation.chatSettings?.pinnedMessage?.text)
    }

    companion object {
        fun newInstance(peerId: Int): ConversationChatOwnerFragment {
            val fragment = ConversationChatOwnerFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}