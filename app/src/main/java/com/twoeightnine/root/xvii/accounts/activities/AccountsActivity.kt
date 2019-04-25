package com.twoeightnine.root.xvii.accounts.activities

import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.accounts.fragments.AccountsFragment
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class AccountsActivity : ContentActivity() {

    override fun getFragment(args: Bundle?) = AccountsFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, AccountsActivity::class.java)
        }
    }
}