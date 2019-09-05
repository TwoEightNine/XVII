package com.twoeightnine.root.xvii.chatowner

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.activities.ContentActivity

class ChatOwnerActivity : ContentActivity() {

    override fun createFragment(intent: Intent?): Fragment {
        val peerId = intent?.extras?.getInt(BaseChatOwnerFragment.ARG_PEER_ID) ?: 0

        return when {
            else -> UserChatOwnerFragment.newInstance(peerId)
        }
    }

    companion object {

        fun launch(context: Context?, peerId: Int) {
            context?.startActivity(Intent(context, ChatOwnerActivity::class.java).apply {
                putExtra(BaseChatOwnerFragment.ARG_PEER_ID, peerId)
            })
        }
    }
}