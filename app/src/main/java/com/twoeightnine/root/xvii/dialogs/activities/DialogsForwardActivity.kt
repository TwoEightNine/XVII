package com.twoeightnine.root.xvii.dialogs.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment

class DialogsForwardActivity : ContentActivity() {

    override fun getFragment(args: Bundle?) = DialogsForwardFragment.newInstance(
            args?.getString(DialogsForwardFragment.ARG_FORWARDED) ?: ""
    )

    companion object {
        fun launch(fragment: Fragment?, requestCode: Int, forwarded: String) {
            if (fragment?.context == null) return

            fragment.startActivityForResult(Intent(fragment.context, DialogsForwardActivity::class.java).apply {
                putExtra(DialogsForwardFragment.ARG_FORWARDED, forwarded)
            }, requestCode)
        }
    }
}