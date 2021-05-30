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

package com.twoeightnine.root.xvii.chatowner.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerFactory
import com.twoeightnine.root.xvii.chatowner.MembersAdapter
import com.twoeightnine.root.xvii.chats.messages.deepforwarded.DeepForwardedFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Conversation
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.showWarnConfirm
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import global.msnthrp.xvii.uikit.extensions.lowerIf
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.fragment_chat_owner_conversation.*

class ConversationChatOwnerFragment : BaseChatOwnerFragment<Conversation>() {

    private val adapter by lazy {
        MembersAdapter(context ?: return@lazy null, ::onUserClick, ::onUserLongClick)
    }

    override fun getLayoutId() = R.layout.fragment_chat_owner_conversation

    override fun getChatOwnerClass() = Conversation::class.java

    override fun bindChatOwner(chatOwner: Conversation?) {
        val conversation = chatOwner ?: return

        fabOpenChat.setVisible(conversation.canWrite?.allowed != false)
        addValue(R.drawable.ic_pinned, conversation.chatSettings?.pinnedMessage?.text, {
            conversation.chatSettings?.pinnedMessage?.id?.also { id ->
                startFragment<DeepForwardedFragment>(DeepForwardedFragment.createArgs(id))
            }
        })
        viewModel.loadChatMembers(conversation.getPeerId())
        btnLeave.setOnClickListener { onLeaveGroupClick() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvUsers.layoutManager = LinearLayoutManager(context)
        rvUsers.adapter = adapter

        ivEdit.setOnClickListener { showTitleDialog() }
        ivEdit.paint(Munch.color.color50)
    }

    override fun getBottomPaddableView(): View = vBottom

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.conversationMembers.observe(viewLifecycleOwner, Observer(::onMembersLoaded))
    }

    private fun onMembersLoaded(profiles: List<User>) {
        adapter?.update(profiles)
    }

    private fun onUserClick(user: User) {
        ChatOwnerFactory.launch(context, user.id)
    }

    private fun onUserLongClick(user: User) {
        val peerId = getChatOwner()?.getPeerId() ?: 0
        var name = user.fullName.lowerIf(Prefs.lowerTexts)
        showWarnConfirm(context, getString(R.string.wanna_kick_user, name), getString(R.string.kick_user)) { confirmed ->
            if (confirmed) {
                viewModel.kickUser(peerId, user.id)
            }
        }
    }

    private fun onLeaveGroupClick() {
        val peerId = getChatOwner()?.getPeerId() ?: 0
        showWarnConfirm(context, getString(R.string.wanna_leave_conversation), getString(R.string.leave_conversation)) { confirmed ->
            if (confirmed) {
                viewModel.leaveConversation(peerId)
            }
        }
    }

    private fun showTitleDialog() {
        val context = context ?: return
        val chatOwner = getChatOwner() ?: return
        val oldTitle = chatOwner.getTitle()

        TextInputAlertDialog(context, getString(R.string.new_title), oldTitle) { newTitle ->
            viewModel.changeChatTitle(chatOwner.getPeerId(), newTitle)
        }.show()
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