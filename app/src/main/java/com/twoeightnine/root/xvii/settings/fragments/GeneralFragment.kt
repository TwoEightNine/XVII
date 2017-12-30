package com.twoeightnine.root.xvii.settings.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.CacheHelper
import com.twoeightnine.root.xvii.utils.showCommon

/**
 * Created by root on 2/2/17.
 */

class GeneralFragment : BaseFragment() {

    @BindView(R.id.llContainer)
    lateinit var llContainer: LinearLayout
    @BindView(R.id.switchOffline)
    lateinit var swOffline: Switch
    @BindView(R.id.switchRead)
    lateinit var swRead: Switch
    @BindView(R.id.switchTyping)
    lateinit var swTyping: Switch
    @BindView(R.id.switchManualUpd)
    lateinit var swManual: Switch
    @BindView(R.id.tvClearCache)
    lateinit var tvClearCache: TextView

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
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
        swOffline.isChecked = Prefs.beOffline
        swRead.isChecked = Prefs.markAsRead
        swTyping.isChecked = Prefs.showTyping
        swManual.isChecked = Prefs.manualUpdating
    }

    private fun saveSwitches() {
        Prefs.beOffline = swOffline.isChecked
        Prefs.markAsRead = swRead.isChecked
        Prefs.showTyping = swTyping.isChecked
        Prefs.manualUpdating = swManual.isChecked
    }

    override fun onStop() {
        super.onStop()
        saveSwitches()
    }

    override fun getLayout() = R.layout.fragment_general
}
