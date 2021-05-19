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