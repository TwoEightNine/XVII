package com.twoeightnine.root.xvii.settings.fragments

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.CacheHelper
import com.twoeightnine.root.xvii.utils.showCommon
import kotlinx.android.synthetic.main.fragment_general.*

/**
 * Created by root on 2/2/17.
 */

class GeneralFragment : BaseFragment() {

    override fun bindViews(view: View) {
        initSwitches()
        tvClearCache.setOnClickListener {
            with (CacheHelper) {
                deleteAllUsersAsync()
                deleteAllGroupsAsync()
                deleteAllMessagesAsync()
            }
            showCommon(activity, R.string.cache_cleared)
        }
        Style.forAll(llContainer)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.general))
    }

    private fun initSwitches() {
        switchOffline.isChecked = Prefs.beOffline
        switchRead.isChecked = Prefs.markAsRead
        switchTyping.isChecked = Prefs.showTyping
        switchManualUpd.isChecked = Prefs.manualUpdating
        switchStoreKeys.isChecked = Prefs.storeCustomKeys
    }

    private fun saveSwitches() {
        Prefs.beOffline = switchOffline.isChecked
        Prefs.markAsRead = switchRead.isChecked
        Prefs.showTyping = switchTyping.isChecked
        Prefs.manualUpdating = switchManualUpd.isChecked
        Prefs.storeCustomKeys = switchStoreKeys.isChecked
    }

    override fun onStop() {
        super.onStop()
        saveSwitches()
    }

    override fun getLayout() = R.layout.fragment_general
}
