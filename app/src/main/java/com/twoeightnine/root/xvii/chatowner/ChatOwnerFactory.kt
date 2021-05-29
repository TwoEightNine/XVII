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

package com.twoeightnine.root.xvii.chatowner

import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity
import com.twoeightnine.root.xvii.chatowner.fragments.BaseChatOwnerFragment
import com.twoeightnine.root.xvii.chatowner.fragments.ConversationChatOwnerFragment
import com.twoeightnine.root.xvii.chatowner.fragments.GroupChatOwnerFragment
import com.twoeightnine.root.xvii.chatowner.fragments.UserChatOwnerFragment
import com.twoeightnine.root.xvii.utils.matchesChatId
import com.twoeightnine.root.xvii.utils.matchesGroupId


object ChatOwnerFactory {

    fun launch(context: Context?, peerId: Int) {
        val args = Bundle().apply {
            putInt(BaseChatOwnerFragment.ARG_PEER_ID, peerId)
        }
        val fragmentClass = when {
            peerId.matchesChatId() -> ConversationChatOwnerFragment::class.java
            peerId.matchesGroupId() -> GroupChatOwnerFragment::class.java
            else -> UserChatOwnerFragment::class.java
        }
        FragmentPlacementActivity.launch(context, fragmentClass, args)
    }
}