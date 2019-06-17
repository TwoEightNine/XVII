package com.twoeightnine.root.xvii.features.general

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.showToast
import com.twoeightnine.root.xvii.utils.stylizeAll
import kotlinx.android.synthetic.main.fragment_general.*

/**
 * Created by root on 2/2/17.
 */

class GeneralFragment : BaseOldFragment() {

    override fun bindViews(view: View) {
        initSwitches()
        tvClearCache.setOnClickListener {
            showToast(activity, R.string.cache_cleared)
        }
        llContainer.stylizeAll()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.general))
    }

    private fun initSwitches() {
        switchOffline.isChecked = Prefs.beOffline
        switchRead.isChecked = Prefs.markAsRead
        switchTyping.isChecked = Prefs.showTyping
        switchShowSeconds.isChecked = Prefs.showSeconds
        switchLowerTexts.isChecked = Prefs.lowerTexts
        switchAppleEmojis.isChecked = Prefs.appleEmojis
        switchShowStickers.isChecked = Prefs.showStickers
        switchShowVoice.isChecked = Prefs.showVoice
        switchStoreKeys.isChecked = Prefs.storeCustomKeys
    }

    private fun saveSwitches() {
        Prefs.beOffline = switchOffline.isChecked
        Prefs.markAsRead = switchRead.isChecked
        Prefs.showTyping = switchTyping.isChecked
        Prefs.showSeconds = switchShowSeconds.isChecked
        Prefs.lowerTexts = switchLowerTexts.isChecked
        Prefs.appleEmojis = switchAppleEmojis.isChecked
        Prefs.showStickers = switchShowStickers.isChecked
        Prefs.showVoice = switchShowVoice.isChecked
        Prefs.storeCustomKeys = switchStoreKeys.isChecked
    }

    override fun onStop() {
        super.onStop()
        saveSwitches()
    }

    override fun getLayout() = R.layout.fragment_general

    companion object {

        fun newInstance() = GeneralFragment()
    }
}
