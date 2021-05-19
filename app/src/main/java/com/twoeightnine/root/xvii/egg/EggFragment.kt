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

package com.twoeightnine.root.xvii.egg

import android.os.Bundle
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.extensions.load
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetMargin
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.fragment_egg.*

/**
 * Created by chuck palahniuk on 8/18/17.
 */

class EggFragment : BaseFragment() {

    private val egg by lazy {
        arguments?.getSerializable(ARG_EGG) as? Egg
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (val egg = egg ?: Egg.getRandomEgg()) {
            is Egg.ImageEgg -> {
                ivImage.load(egg.imageUrl, placeholder = false) {
                    fitCenter()
                }
            }
            is Egg.ImageAndTextEgg -> {
                ivImage.load(egg.imageUrl, placeholder = false) {
                    centerCrop()
                }
                tvText.applyBottomInsetMargin()
                tvText.show()
                tvText.setText(egg.textRes)
            }
        }
    }

    override fun getLayoutId() = R.layout.fragment_egg

    companion object {

        private const val ARG_EGG = "egg"

        fun createArgs(egg: Egg) = Bundle().apply {
            putSerializable(ARG_EGG, egg)
        }
    }
}