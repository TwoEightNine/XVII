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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.lg.L
import global.msnthrp.xvii.uikit.utils.DisplayUtils

class FullScreenImageAdapter(
        private val activity: Activity,
        private val urls: ArrayList<String>,
        private val callback: TouchImageView.InteractionCallback,
        private val sizes: List<Size>? = null
) : androidx.viewpager.widget.PagerAdapter() {

    private lateinit var inflater: LayoutInflater
    private val tivManager = ActiveTivManager(3)

    override fun getCount() = urls.size

    override fun isViewFromObject(view: View, any: Any) = view === any

    @SuppressLint("CheckResult")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewLayout = inflater.inflate(R.layout.item_fullscreen_image, container, false)
        val imgDisplay = viewLayout.findViewById<TouchImageView>(R.id.tivImage)

        val url = urls[position]
        val fromFile = url.startsWith("file://")

        var optimalViewWidth: Int? = null
        sizes?.getOrNull(position)?.also { size ->
            val imageRatio = size.height.toFloat() / size.width
            val screenHeight = DisplayUtils.screenHeight
            val displayedWidth = screenHeight.toFloat() / imageRatio
            val maxDisplayedWidth = (TouchImageView.MAX_SCALE * displayedWidth).toInt() + 1
            if (maxDisplayedWidth < DisplayUtils.screenWidth) {
                optimalViewWidth = maxDisplayedWidth
            }
            L.tag(TAG).log("ir = $imageRatio, sh = $screenHeight, iw = $displayedWidth, optw = $optimalViewWidth")
        }
        optimalViewWidth?.also { newWidth ->
            imgDisplay?.layoutParams?.apply {
                width = newWidth
                imgDisplay.layoutParams = this
            }
        }

        tivManager.saveTiv(position, imgDisplay)
        imgDisplay.callback = callback

        imgDisplay.load(urls[position], placeholder = false) {
            if (fromFile) {
                var (width, height) = getImageSize(url)
                val ratio = width.toFloat() / height.toFloat()
                if (width > height) {
                    width = 3000
                    height = (width / ratio).toInt()
                } else {
                    height = 3000
                    width = (ratio * height).toInt()
                }

                override(width, height)
            }
            skipMemoryCache(true)
        }

        container.addView(viewLayout)
        return viewLayout
    }

    fun canSlide(pos: Int) = tivManager.getTiv(pos)?.canSlide()

    private fun getImageSize(path: String): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        return Pair(imageWidth, imageHeight)
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as RelativeLayout)
    }

    companion object {
        private const val TAG = "fullscreen"
    }
}
