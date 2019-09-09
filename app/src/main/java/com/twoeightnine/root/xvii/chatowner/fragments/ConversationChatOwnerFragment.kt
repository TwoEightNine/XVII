package com.twoeightnine.root.xvii.chatowner.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.chatowner.MembersAdapter
import com.twoeightnine.root.xvii.model.Conversation
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.showConfirm
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
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
        addValue(R.drawable.ic_pinned, conversation.chatSettings?.pinnedMessage?.text)
        viewModel.loadChatMembers(conversation.getPeerId())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvUsers.layoutManager = LinearLayoutManager(context)
        rvUsers.adapter = adapter

        ivEdit.setOnClickListener { showTitleDialog() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.conversationMembers.observe(viewLifecycleOwner, Observer(::onMembersLoaded))
    }

    private fun onMembersLoaded(profiles: List<User>) {
        adapter?.update(profiles)
    }

    private fun onUserClick(user: User) {
        ChatOwnerActivity.launch(context, user.id)
    }

    private fun onUserLongClick(user: User) {
        val peerId = getChatOwner()?.getPeerId() ?: 0
        showConfirm(context, "") { confirmed ->
            if (confirmed) {
                viewModel.kickUser(peerId, user.id)
            }
        }
    }

    private fun showTitleDialog() {
        val context = context ?: return
        val chatOwner = getChatOwner() ?: return
        val oldTitle = chatOwner.getTitle()

        TextInputAlertDialog(context, getString(R.string.new_title), oldTitle) { newTitle ->
            viewModel.changeChatTitle(chatOwner.getPeerId(), newTitle)
        }
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