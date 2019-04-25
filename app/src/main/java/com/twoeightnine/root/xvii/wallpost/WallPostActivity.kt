package com.twoeightnine.root.xvii.wallpost

import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class WallPostActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun getFragment(args: Bundle?) = WallPostFragment.newInstance(
            args?.getString(WallPostFragment.ARG_POST_ID) ?: ""
    )

    companion object {
        fun launch(context: Context?, postId: String) {
            launchActivity(context, WallPostActivity::class.java) {
                putExtra(WallPostFragment.ARG_POST_ID, postId)
            }
        }
    }
}