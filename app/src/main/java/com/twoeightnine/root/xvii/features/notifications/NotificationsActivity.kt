package com.twoeightnine.root.xvii.features.notifications

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class NotificationsActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = NotificationsFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, NotificationsActivity::class.java)
        }
    }
}