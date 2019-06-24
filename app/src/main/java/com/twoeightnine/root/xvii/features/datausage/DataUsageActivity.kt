package com.twoeightnine.root.xvii.features.datausage

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.activities.ContentActivity

class DataUsageActivity : ContentActivity() {

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, DataUsageActivity::class.java))
        }
    }

    override fun createFragment(intent: Intent?) = DataUsageFragment.newInstance()
}