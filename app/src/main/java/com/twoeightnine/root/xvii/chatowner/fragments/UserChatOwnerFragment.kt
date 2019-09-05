package com.twoeightnine.root.xvii.chatowner.fragments

import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_chat_owner_user.*

class UserChatOwnerFragment : BaseChatOwnerFragment<User>() {

    override fun getLayoutId() = R.layout.fragment_chat_owner_user

    override fun getChatOwnerClass() = User::class.java

    override fun bindChatOwner(chatOwner: User?) {
        val user = chatOwner ?: return

        fabOpenChat.setVisible(user.canWriteThisUser)
        addValue(R.drawable.ic_quotation, user.status, null) {
            copy(user.status, R.string.status)
        }
        addValue(R.drawable.ic_calendar, formatDate(formatBdate(user.bdate)).toLowerCase())
        addValue(R.drawable.ic_pin_home, user.city?.title)
        addValue(R.drawable.ic_home, user.hometown)
        addValue(R.drawable.ic_phone, user.mobilePhone, { callIntent(context, user.mobilePhone) }) {
            copy(user.mobilePhone, R.string.mphone)
        }
        addValue(R.drawable.ic_phone, user.homePhone, { callIntent(context, user.homePhone) }) {
            copy(user.homePhone, R.string.hphone)
        }
        addValue(0, getRelation(context, user.relation))
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