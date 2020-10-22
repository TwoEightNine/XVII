package com.twoeightnine.root.xvii.chatowner.fragments

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.chat.secret.SecretChatActivity
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_chat_owner_user.*

class UserChatOwnerFragment : BaseChatOwnerFragment<User>() {

    override fun getLayoutId() = R.layout.fragment_chat_owner_user

    override fun getChatOwnerClass() = User::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSecretChat.setOnClickListener {
            getChatOwner()?.also {
                SecretChatActivity.launch(context, it)
            }
        }
        btnBlockUser.setOnClickListener {
            showWarnConfirm(context, getString(R.string.block_user_confirmation), getString(R.string.block_user)) { confirmed ->
                if (confirmed) {
                    viewModel.blockUser(getChatOwner()?.getPeerId() ?: 0)
                }
            }
        }
        btnUnblockUser.setOnClickListener {
            viewModel.unblockUser(getChatOwner()?.getPeerId() ?: 0)
        }
        btnSecretChat.stylize()

    }

    override fun getBottomPaddableView(): View = vBottom

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.blocked.observe(viewLifecycleOwner, ::onBlockedChanged)
        viewModel.foaf.observe(viewLifecycleOwner) { foaf ->
            foaf.data?.also { registrationDate ->
                val registrationTs = (registrationDate.time / 1000L).toInt()
                addValue(R.drawable.ic_registration_date, getDate(registrationTs))
            }
        }
    }

    override fun bindChatOwner(chatOwner: User?) {
        val user = chatOwner ?: return

        fabOpenChat.setVisible(user.canWriteThisUser)
        addValue(R.drawable.ic_quotation, user.status, null) {
            copy(user.status, R.string.status)
        }
        user.counters?.friends?.also { count ->
            if (count != 0) {
                val number = shortifyNumber(count)
                addValue(R.drawable.ic_friends_popup, resources.getQuantityString(R.plurals.friends, count, number))
            }
        }
        user.counters?.mutual?.also { count ->
            if (count != 0) {
                addValue(R.drawable.ic_mutual_friends, resources.getQuantityString(R.plurals.mutual_friends, count, count))
            }
        }
        user.counters?.followers?.also { count ->
            if (count != 0) {
                val number = shortifyNumber(count)
                addValue(R.drawable.ic_followers, resources.getQuantityString(R.plurals.followers, count, number))
            }
        }
        addValue(R.drawable.ic_birth_date, formatDate(formatBdate(user.bdate)).toLowerCase())
        addValue(R.drawable.ic_pin_home, user.city?.title)
        addValue(R.drawable.ic_home, user.hometown)
        addValue(R.drawable.ic_phone, user.mobilePhone, { callIntent(context, user.mobilePhone) }) {
            copy(user.mobilePhone, R.string.mphone)
        }
        addValue(R.drawable.ic_phone, user.homePhone, { callIntent(context, user.homePhone) }) {
            copy(user.homePhone, R.string.hphone)
        }
        addValue(R.drawable.ic_relation, getRelation(context, user.relation))
        addValue(R.drawable.ic_worldwide, user.site, { goTo(user.site) }) {
            copy(user.site, R.string.site)
        }

        addValue(R.drawable.ic_vk, user.domain, null) {
            copy(user.link, R.string.link)
        }
        addValue(R.drawable.ic_inst, user.instagram, { goTo(user.linkInst) }) {
            copy(user.linkInst, R.string.instagram)
        }
        addValue(R.drawable.ic_twitter, user.twitter, { goTo(user.linkTwitter) }) {
            copy(user.linkTwitter, R.string.twitter)
        }
        addValue(R.drawable.ic_fb, user.facebook, null) {
            copy(user.facebook, R.string.facebook)
        }
        viewModel.loadFoaf(chatOwner.getPeerId())
    }

    private fun onBlockedChanged(data: Wrapper<Boolean>) {
        if (data.data != null) {
            btnBlockUser.setVisible(!data.data)
            btnUnblockUser.setVisible(data.data)
        } else {
            showError(context, data.error ?: "")
        }
    }

    companion object {

        fun newInstance(peerId: Int): UserChatOwnerFragment {
            val fragment = UserChatOwnerFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}