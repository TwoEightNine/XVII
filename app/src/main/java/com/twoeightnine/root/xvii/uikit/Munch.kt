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

package com.twoeightnine.root.xvii.uikit

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import global.msnthrp.xvii.uikit.utils.color.Rgb
import global.msnthrp.xvii.uikit.utils.color.addHue
import global.msnthrp.xvii.uikit.utils.color.toHsv
import global.msnthrp.xvii.uikit.utils.color.toRgb

object Munch {

    private const val NEAR = 30
    private const val SIMILAR = 60
    private const val RANGE = 90

    private val backgroundLight = App.context.getColor(R.color.background_light)
    private val backgroundDark = App.context.getColor(R.color.background_dark)


    val isLightTheme = Prefs.isLightTheme

    val color = ColorScope(Prefs.color)

    val nextNear = color.color.getNextNear()
    val prevNear = color.color.getPrevNear()

    val nearColors: IntArray by lazy {
        val color = color.color
        intArrayOf(
                color,
                color.getNextNear(),
                color,
                color.getPrevNear()
        )
    }

    val nextAnalogy: IntArray by lazy {
        val color = color.color
        intArrayOf(
                color,
                color.getNextNear(),
                color.getNextSimilar(),
                color.getNextRange()
        )
    }

    private fun Int.alphaOnWhite(alpha: Float = 0.5f) = mixWith(backgroundLight, alpha)
    private fun Int.alphaOnBlack(alpha: Float = 0.5f) = mixWith(backgroundDark, alpha)

    private fun Int.getNextNear() = toHsv().addHue(NEAR).toInt()
    private fun Int.getNextSimilar() = toHsv().addHue(SIMILAR).toInt()
    private fun Int.getNextRange() = toHsv().addHue(RANGE).toInt()

    private fun Int.getPrevNear() = toHsv().addHue(-NEAR).toInt()

    private fun Int.mixWith(color: Int, alpha: Float = 0.5f): Int {
        return when (alpha) {
            0f -> color
            1f -> this
            else -> {
                val rgb = toRgb()
                val colorRgb = color.toRgb()
                val mixedRgb = Rgb(
                        r = (rgb.r * alpha + colorRgb.r * (1 - alpha)).toInt(),
                        g = (rgb.g * alpha + colorRgb.g * (1 - alpha)).toInt(),
                        b = (rgb.b * alpha + colorRgb.b * (1 - alpha)).toInt()
                )
                mixedRgb.toInt()
            }
        }
    }

    data class ColorScope(val color: Int) {

        private val colorOnWhiteCache = mutableMapOf<Int, Int>()
        private val colorOnDarkCache = mutableMapOf<Int, Int>()

        val color50: Int
            get() = color(50)

        val color20: Int
            get() = color(20)

        fun color(useCase: UseCase, theme: Theme = Theme.DEFAULT): Int {
            return when (theme) {
                Theme.DEFAULT -> color(useCase.alpha)
                Theme.WHITE -> colorWhite(useCase.alpha)
                Theme.DARK -> colorDark(useCase.alpha)
            }
        }

        fun color(alphaOnBackground: Int) = when {
            isLightTheme -> colorWhite(alphaOnBackground)
            else -> colorDark(alphaOnBackground)
        }

        fun colorWhite(alphaOnBackground: Int): Int {
            return getColorInternal(alphaOnBackground, colorOnWhiteCache) {
                color.alphaOnWhite(it)
            }
        }

        fun colorDark(alphaOnBackground: Int): Int {
            return getColorInternal(alphaOnBackground, colorOnDarkCache) {
                color.alphaOnBlack(it)
            }
        }

        private fun getColorInternal(alpha: Int, cache: MutableMap<Int, Int>, colorGetter: (Float) -> Int): Int {
            return cache.getOrPut(alpha) {
                colorGetter(alpha * 0.01f)
            }
        }
    }

    enum class UseCase(val alpha: Int) {
        MESSAGES_IN(5),
        MESSAGES_OUT(17)
    }

    enum class Theme {
        DEFAULT,
        WHITE,
        DARK
    }

}
