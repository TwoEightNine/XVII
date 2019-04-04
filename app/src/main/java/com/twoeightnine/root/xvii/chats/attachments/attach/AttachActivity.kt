package com.twoeightnine.root.xvii.chats.attachments.attach

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity

class AttachActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun getFragment(args: Bundle?) = AttachFragment.newInstance()

    companion object {

        fun launch(activity: Activity?, requestCode: Int) {
            if (activity == null) return

            activity.startActivityForResult(Intent(activity, AttachActivity::class.java), requestCode)
        }
    }
}