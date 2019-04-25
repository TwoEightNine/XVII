package com.twoeightnine.root.xvii.features.notifications

import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class NotificationsActivity : ContentActivity() {

    override fun getFragment(args: Bundle?) = NotificationsFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, NotificationsActivity::class.java)
        }
    }
}