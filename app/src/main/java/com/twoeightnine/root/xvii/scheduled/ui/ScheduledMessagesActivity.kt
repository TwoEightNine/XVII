package com.twoeightnine.root.xvii.scheduled.ui

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.activities.ContentActivity

class ScheduledMessagesActivity : ContentActivity() {

    override fun createFragment(intent: Intent?): Fragment =
            ScheduledMessagesFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, ScheduledMessagesActivity::class.java))
        }
    }
}