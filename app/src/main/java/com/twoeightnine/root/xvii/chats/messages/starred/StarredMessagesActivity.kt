package com.twoeightnine.root.xvii.chats.messages.starred

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class StarredMessagesActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = StarredMessagesFragment.newInstance()

    companion object {

        fun launch(context: Context?) {
            launchActivity(context, StarredMessagesActivity::class.java)
        }
    }
}