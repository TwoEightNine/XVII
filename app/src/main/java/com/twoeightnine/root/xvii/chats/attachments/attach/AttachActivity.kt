package com.twoeightnine.root.xvii.chats.attachments.attach

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.main.InsetViewModel
import com.twoeightnine.root.xvii.utils.consumeInsets
import kotlinx.android.synthetic.main.activity_content.*

class AttachActivity : ContentActivity() {

    private val insetViewModel by lazy {
        ViewModelProviders.of(this)[InsetViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?) = AttachFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vShadow.consumeInsets { top, bottom ->
            insetViewModel.updateTopInset(top)
            insetViewModel.updateBottomInset(bottom)
        }
    }

    companion object {

        fun launch(fragment: Fragment?, requestCode: Int) {
            if (fragment?.context == null) return

            fragment.startActivityForResult(Intent(fragment.context, AttachActivity::class.java), requestCode)
        }
    }
}