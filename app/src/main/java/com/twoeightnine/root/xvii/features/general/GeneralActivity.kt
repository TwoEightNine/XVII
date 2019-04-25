package com.twoeightnine.root.xvii.features.general

import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class GeneralActivity : ContentActivity() {

    override fun getFragment(args: Bundle?) = GeneralFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, GeneralActivity::class.java)
        }
    }
}