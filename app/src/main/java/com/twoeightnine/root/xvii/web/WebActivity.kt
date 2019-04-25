package com.twoeightnine.root.xvii.web

import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class WebActivity : ContentActivity() {

    override fun getFragment(args: Bundle?) = WebFragment.newInstance(
            args?.getString(WebFragment.ARG_URL) ?: "",
            args?.getString(WebFragment.ARG_TITLE) ?: ""
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