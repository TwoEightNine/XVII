/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.features.general

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.getSize
import com.twoeightnine.root.xvii.utils.showToast
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.fragment_general.*

/**
 * Created by root on 2/2/17.
 */

class GeneralFragment : BaseFragment() {

    private val viewModel by viewModels<GeneralViewModel>()

    override fun getLayoutId() = R.layout.fragment_general

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSwitches()
        btnClearCache.setOnClickListener {
            viewModel.clearCache()
        }
        btnRefreshStickers.setOnClickListener {
            viewModel.refreshStickers()
        }
        svContent.applyBottomInsetPadding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.calculateCacheSize()

        viewModel.cacheSize.observe(viewLifecycleOwner) { size ->
            context?.resources?.also {
                tvCacheSize.text = getString(R.string.cache_size, getSize(it, size.toInt()))
            }
        }
        viewModel.stickersRefreshing.observe(viewLifecycleOwner) { loading ->
            btnRefreshStickers.setVisible(!loading)
            if (!loading) {
                showToast(context, R.string.stickers_refreshed)
            }
        }
    }

    private fun initSwitches() {
        switchOffline.isChecked = Prefs.beOffline
        switchOnline.isChecked = Prefs.beOnline
        switchHideStatus.isChecked = Prefs.hideStatus
        switchRead.isChecked = Prefs.markAsRead
        switchTyping.isChecked = Prefs.showTyping
        switchSendByEnter.isChecked = Prefs.sendByEnter
        switchStickerSuggestions.isChecked = Prefs.stickerSuggestions
        switchExactSuggestions.isChecked = Prefs.exactSuggestions
        switchSwipeToBack.isChecked = Prefs.enableSwipeToBack
        switchStoreKeys.isChecked = Prefs.storeCustomKeys
        switchLiftKeyboard.isChecked = Prefs.liftKeyboard
        switchSuggestPeople.isChecked = Prefs.suggestPeople

        switchOffline.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchOnline.isChecked = false
        }
        switchOnline.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchOffline.isChecked = false
        }
        switchHideStatus.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            viewModel.setHideMyStatus(isChecked)
        }
        switchStickerSuggestions.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            switchExactSuggestions.setVisible(isChecked)
        }
    }

    private fun saveSwitches() {
        Prefs.beOffline = switchOffline.isChecked
        Prefs.beOnline = switchOnline.isChecked
        Prefs.hideStatus = switchHideStatus.isChecked
        Prefs.markAsRead = switchRead.isChecked
        Prefs.showTyping = switchTyping.isChecked
        Prefs.sendByEnter = switchSendByEnter.isChecked
        Prefs.stickerSuggestions = switchStickerSuggestions.isChecked
        Prefs.exactSuggestions = switchExactSuggestions.isChecked
        Prefs.enableSwipeToBack = switchSwipeToBack.isChecked
        Prefs.storeCustomKeys = switchStoreKeys.isChecked
        Prefs.liftKeyboard = switchLiftKeyboard.isChecked
        Prefs.suggestPeople = switchSuggestPeople.isChecked
    }

    override fun onStop() {
        super.onStop()
        saveSwitches()
    }

    companion object {

        fun newInstance() = GeneralFragment()
    }
}
