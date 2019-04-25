package com.twoeightnine.root.xvii.chats.messages.starred

import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class StarredMessagesActivity : ContentActivity() {

    override fun getFragment(args: Bundle?) = StarredMessagesFragment.newInstance()

    companion object {

        fun launch(context: Context?) {
            launchActivity(context, StarredMessagesActivity::class.java)
        }
    }
}