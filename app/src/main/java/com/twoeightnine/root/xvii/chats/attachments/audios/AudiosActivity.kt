package com.twoeightnine.root.xvii.chats.attachments.audios

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.managers.Session

class AudiosActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?) = AudioAttachmentsFragment.newInstance(Session.uid)

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, AudiosActivity::class.java))
        }
    }
}