package com.twoeightnine.root.xvii.chatowner.fragments

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.Group
import kotlinx.android.synthetic.main.fragment_chat_owner_conversation.*

class GroupChatOwnerFragment : BaseChatOwnerFragment<Group>() {

    override fun getLayoutId() = R.layout.fragment_chat_owner_group

    override fun getChatOwnerClass() = Group::class.java

    override fun bindChatOwner(chatOwner: Group?) {
        val group = chatOwner ?: return

        addValue(R.drawable.ic_quotation, group.status)
        addValue(R.drawable.ic_sheet, group.description)
        addValue(R.drawable.ic_vk, group.screenName)
    }

    override fun getBottomPaddableView(): View = vBottom

    companion object {
        fun newInstance(peerId: Int): GroupChatOwnerFragment {
            val fragment = GroupChatOwnerFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}