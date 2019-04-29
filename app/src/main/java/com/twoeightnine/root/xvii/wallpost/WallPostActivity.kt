package com.twoeightnine.root.xvii.wallpost

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class WallPostActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?) = WallPostFragment.newInstance(
            intent?.extras?.getString(WallPostFragment.ARG_POST_ID) ?: ""
    )

    companion object {
        fun launch(context: Context?, postId: String) {
            launchActivity(context, WallPostActivity::class.java) {
                putExtra(WallPostFragment.ARG_POST_ID, postId)
            }
        }
    }
}