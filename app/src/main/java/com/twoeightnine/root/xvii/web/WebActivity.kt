package com.twoeightnine.root.xvii.web

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class WebActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = WebFragment.newInstance(
            intent?.extras?.getString(WebFragment.ARG_URL) ?: "",
            intent?.extras?.getString(WebFragment.ARG_TITLE) ?: ""
    )

    companion object {
        fun launch(context: Context?, url: String, title: String) {
            launchActivity(context, WebActivity::class.java) {
                putExtra(WebFragment.ARG_URL, url)
                putExtra(WebFragment.ARG_TITLE, title)
            }
        }
    }
}