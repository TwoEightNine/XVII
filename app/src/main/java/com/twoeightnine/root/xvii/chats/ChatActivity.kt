package com.twoeightnine.root.xvii.chats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment

class ChatActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun getFragment(args: Bundle?): Fragment {
        val userId = args?.getInt(USER_ID) ?: 0
        val title = args?.getString(TITLE) ?: ""
        val forwarded = args?.getString(FORWARDED) ?: ""
        val isOnline = args?.getBoolean(IS_ONLINE) == true
        val avatar = args?.getString(AVATAR)
//        val message = Message(
//                0, 0, userId, 0, 0, title, "", null
//        )
//        if (userId > 2000000000) {
//            message.chatId = userId - 2000000000
//        }
        return ChatFragment.newInstance(userId, title, avatar, isOnline)
//        return ChatMessagesFragment.newInstance(userId)
    }

    companion object {
        const val USER_ID = "userId"
        const val TITLE = "title"
        const val IS_ONLINE = "isOnline"
        const val FORWARDED = "forwarded"
        const val AVATAR = "avatar"

        fun launch(context: Context?, userId: Int, title: String, avatar: String? = null,
                   isOnline: Boolean = false, forwarded: String = "") {
            context ?: return

            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                putExtra(USER_ID, userId)
                putExtra(TITLE, title)
                putExtra(FORWARDED, forwarded)
                putExtra(IS_ONLINE, isOnline)
                putExtra(AVATAR, avatar)
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
    }
}