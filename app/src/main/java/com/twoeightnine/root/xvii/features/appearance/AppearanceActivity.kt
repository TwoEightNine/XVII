package com.twoeightnine.root.xvii.features.appearance

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class AppearanceActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = AppearanceFragment.newInstance()

    override fun onBackPressed() {
        val fragment = getFragment() as? AppearanceFragment
        if (fragment != null && fragment.hasChanges()) {
            fragment.askForRestarting()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, AppearanceActivity::class.java)
        }
    }
}