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

package com.twoeightnine.root.xvii.photoviewer

import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.item_fullscreen_image.view.*
import kotlin.math.abs

/**
 * parallax to [ImageViewerActivity]
 */
class ImagePageTransformer : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        with(page) {
            val absolutePosition = width * -position
            tivImage.translationX = absolutePosition * PARALLAX_COEFF
            val scale = SCALE_COEFF + (1 - SCALE_COEFF) * (1 - abs(position))
            tivImage.scaleX = scale
            tivImage.scaleY = scale
        }
    }

    companion object {

        const val PARALLAX_COEFF = 0.3f
        const val SCALE_COEFF = 0.9f
    }
}