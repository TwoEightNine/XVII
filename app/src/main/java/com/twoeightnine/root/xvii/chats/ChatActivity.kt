package com.twoeightnine.root.xvii.chats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.model.Message

class ChatActivity : ContentActivity() {

    companion object {
        const val USER_ID = "userId"
        const val TITLE = "title"
        const val FORWARDED = "forwarded"

        fun launch(context: Context?, userId: Int, title: String, forwarded: String = "") {
            context ?: return

            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                putExtra(USER_ID, userId)
                putExtra(TITLE, title)
                putExtra(FORWARDED, forwarded)
            })
        }
    }

    override fun getLayoutId() = R.layout.activity_content

    override fun getFragment(args: Bundle?): androidx.fragment.app.Fragment {
        val userId = args?.getInt(USER_ID) ?: 0
        val title = args?.getString(TITLE) ?: ""
        val forwarded = args?.getString(FORWARDED) ?: ""
        val message = Message(
                0, 0, userId, 0, 0, title, "", null
        )
        if (userId > 2000000000) {
            message.chatId = userId - 2000000000
        }
        return ChatFragment.newInstance(message, forwarded)
    }
}