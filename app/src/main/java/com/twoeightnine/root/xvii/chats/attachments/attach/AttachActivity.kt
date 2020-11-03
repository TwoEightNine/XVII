package com.twoeightnine.root.xvii.chats.attachments.attach

import android.content.Intent
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.ContentActivity

class AttachActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?) = AttachFragment.newInstance()

    companion object {

        fun launch(fragment: Fragment?, requestCode: Int) {
            if (fragment?.context == null) return

            fragment.startActivityForResult(Intent(fragment.context, AttachActivity::class.java), requestCode)
        }
    }
}