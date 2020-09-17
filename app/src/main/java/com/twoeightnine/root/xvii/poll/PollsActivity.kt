package com.twoeightnine.root.xvii.poll

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.model.attachments.Poll

class PollsActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = PollFragment.newInstance(intent?.extras)

//    override fun getNavigationBarColor() = Color.TRANSPARENT

    companion object {

        fun launch(context: Context?, poll: Poll) {
            context?.startActivity(Intent(context, PollsActivity::class.java).apply {
                putExtras(PollFragment.getArgs(poll))
            })
        }
    }

}