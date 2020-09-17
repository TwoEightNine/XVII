package com.twoeightnine.root.xvii.accounts.activities

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.accounts.fragments.AccountsFragment
import com.twoeightnine.root.xvii.base.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class AccountsActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = AccountsFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, AccountsActivity::class.java)
        }
    }
}