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

package com.twoeightnine.root.xvii.chats.messages.chat.usual

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.model.User
import global.msnthrp.xvii.data.dialogs.Dialog

class ChatActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?): Fragment {
        val args = intent?.extras
        val forwarded = args?.getString(FORWARDED)
        val shareText = args?.getString(SHARE_TEXT)
        val shareImages = args?.getStringArrayList(SHARE_IMAGE) ?: emptyList<String>()
        val dialog = args?.getParcelable(DIALOG) ?: Dialog(
                peerId = args?.getInt(PEER_ID) ?: 0,
                messageId = args?.getInt(MESSAGE_ID) ?: 0,
                title = args?.getString(TITLE) ?: "",
                photo = args?.getString(AVATAR)
        )
        val search = args?.getBoolean(SEARCH)?: false
        return ChatMessagesFragment.newInstance(dialog, forwarded, shareText, shareImages, search)
    }

    override fun getDraggableBottomMargin(): Int = 200

    override fun getNavigationBarColor() = Color.TRANSPARENT

    companion object {
        const val DIALOG = "dialog"
        const val FORWARDED = "forwarded"
        const val SHARE_TEXT = "shareText"
        const val SHARE_IMAGE = "shareImage"
        const val PEER_ID = "peerId"
        const val MESSAGE_ID = "messageId"
        const val TITLE = "title"
        const val AVATAR = "avatar"
        const val SEARCH = "search"

        fun launch(context: Context?, chatOwner: ChatOwner) {
            launch(context, Dialog(
                    peerId = chatOwner.getPeerId(),
                    title = chatOwner.getTitle(),
                    photo = chatOwner.getAvatar()
            ))
        }

        fun launch(context: Context?, userId: Int, title: String,
                   avatar: String? = null, forwarded: String = "") {
            launch(context, Dialog(
                    peerId = userId,
                    title = title,
                    photo = avatar
            ), forwarded)
        }

        fun launch(context: Context?, dialog: Dialog,
                   forwarded: String? = null, shareText: String? = null,
                   shareImages: List<String> = emptyList()) {
            context ?: return

            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                if (!forwarded.isNullOrEmpty()) {
                    putExtra(FORWARDED, forwarded)
                }
                if (!shareText.isNullOrEmpty()) {
                    putExtra(SHARE_TEXT, shareText)
                }
                if (shareImages.isNotEmpty()) {
                    putStringArrayListExtra(SHARE_IMAGE, ArrayList(shareImages))
                }
                putExtra(DIALOG, dialog)
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }

        fun launch(context: Context?, user: User) {
            launch(context, Dialog(
                    peerId = user.id,
                    title = user.fullName,
                    photo = user.photo100
            ))
        }
        fun launch(context: Context?, dialog: Dialog, search:Boolean = false) {
            context ?: return

            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                putExtra(DIALOG, dialog)
                putExtra(SEARCH, search)
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
    }
}