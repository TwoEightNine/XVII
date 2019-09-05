package com.twoeightnine.root.xvii.chatowner

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.chatowner.fragments.BaseChatOwnerFragment
import com.twoeightnine.root.xvii.chatowner.fragments.GroupChatOwnerFragment
import com.twoeightnine.root.xvii.chatowner.fragments.UserChatOwnerFragment
import com.twoeightnine.root.xvii.utils.matchesGroupId

class ChatOwnerActivity : ContentActivity() {

    override fun createFragment(intent: Intent?): Fragment {
        val peerId = resolvePeerId()

        return when {
            peerId.matchesGroupId() -> GroupChatOwnerFragment.newInstance(peerId)
            else -> UserChatOwnerFragment.newInstance(peerId)
        }
    }

    private fun resolvePeerId(): Int {
        if (intent.action == Intent.ACTION_VIEW) {
            intent?.data?.lastPathSegment?.also { path ->
                return try {
                    Integer.parseInt(path.replace("id", ""))
                } catch (e: NumberFormatException) {
                    0
                }
            }
            return 0
        } else {
            return intent?.extras?.getInt(BaseChatOwnerFragment.ARG_PEER_ID) ?: 0
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