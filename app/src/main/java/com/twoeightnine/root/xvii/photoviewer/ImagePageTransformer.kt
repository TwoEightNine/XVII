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