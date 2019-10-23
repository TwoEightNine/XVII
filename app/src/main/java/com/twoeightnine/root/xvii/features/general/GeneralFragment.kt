package com.twoeightnine.root.xvii.features.general

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_general.*

/**
 * Created by root on 2/2/17.
 */

class GeneralFragment : BaseFragment() {

    private lateinit var viewModel: GeneralViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSwitches()
        btnClearCache.setOnClickListener {
            viewModel.clearCache()
        }
        llContainer.stylizeAll()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.general))
        viewModel = ViewModelProviders.of(this)[GeneralViewModel::class.java]
        viewModel.calculateCacheSize()

        viewModel.cacheCleared.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                showToast(context, R.string.cache_cleared)
            }
        })
        viewModel.cacheSize.observe(viewLifecycleOwner, Observer { size ->
            context?.resources?.also {
                tvCacheSize.text = getString(R.string.cache_size, getSize(it, size.toInt()))
            }
        })
    }

    private fun initSwitches() {
        switchOffline.isChecked = Prefs.beOffline
        switchOnline.isChecked = Prefs.beOnline
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
        Prefs.beOnline = switchOnline.isChecked
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

    override fun getLayoutId() = R.layout.fragment_general

    companion object {

        fun newInstance() = GeneralFragment()
    }
}
