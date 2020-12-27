package com.twoeightnine.root.xvii.chats.attachments.attachments

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.ContentActivity

class AttachmentsActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?)
            = AttachmentsFragment.newInstance(intent?.extras?.getInt(ARG_PEER_ID) ?: 0)

    companion object {

        const val ARG_PEER_ID = "peerId"

        fun launch(context: Context?, peerId: Int) {
            if (context == null) return

            context.startActivity(Intent(context, AttachmentsActivity::class.java).apply {
                putExtra(ARG_PEER_ID, peerId)
            })
        }
    }
}