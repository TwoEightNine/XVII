package com.twoeightnine.root.xvii.chatowner

import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.formatBdate
import com.twoeightnine.root.xvii.utils.formatDate
import com.twoeightnine.root.xvii.utils.getRelation

class UserChatOwnerFragment : BaseChatOwnerFragment<User>() {

    override fun getLayoutId() = R.layout.fragment_chat_owner_user

    override fun getChatOwnerClass() = User::class.java

    override fun bindChatOwner(chatOwner: User?) {
        val user = chatOwner ?: return

        addValue(R.drawable.ic_quotation, user.status)
        addValue(0, formatDate(formatBdate(user.bdate)).toLowerCase())
        addValue(R.drawable.ic_pin_home, user.city?.title)
        addValue(R.drawable.ic_home, user.hometown)
        addValue(R.drawable.ic_phone, user.mobilePhone)
        addValue(R.drawable.ic_phone, user.homePhone)
        addValue(0, getRelation(context, user.relation))
        addValue(R.drawable.ic_worldwide, user.site)

        addValue(R.drawable.ic_vk, user.domain)
        addValue(R.drawable.ic_inst, user.instagram)
        addValue(R.drawable.ic_twitter, user.twitter)
        addValue(R.drawable.ic_fb, user.facebook)
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