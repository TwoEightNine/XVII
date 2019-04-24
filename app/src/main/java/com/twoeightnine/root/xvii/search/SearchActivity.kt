package com.twoeightnine.root.xvii.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity

class SearchActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun getFragment(args: Bundle?) = SearchFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, SearchActivity::class.java))
        }
    }
}