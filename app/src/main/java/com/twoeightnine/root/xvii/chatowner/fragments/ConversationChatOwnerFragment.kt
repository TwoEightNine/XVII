package com.twoeightnine.root.xvii.chatowner.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.chatowner.MembersAdapter
import com.twoeightnine.root.xvii.chats.messages.deepforwarded.DeepForwardedActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Conversation
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.showWarnConfirm
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
        addValue(R.drawable.ic_pinned, conversation.chatSettings?.pinnedMessage?.text, {
            conversation.chatSettings?.pinnedMessage?.id?.also { id ->
                DeepForwardedActivity.launch(context, id)
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
        ChatOwnerActivity.launch(context, user.id)
    }

    private fun onUserLongClick(user: User) {
        val peerId = getChatOwner()?.getPeerId() ?: 0
        var name = user.fullName
        if (Prefs.lowerTexts) {
            name = name.toLowerCase()
        }
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