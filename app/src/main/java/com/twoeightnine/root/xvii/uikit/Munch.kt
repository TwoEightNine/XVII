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
    private const val TRI = 120
    private const val EXTRA = 180

    private val backgroundLight = App.context.getColor(R.color.background_light)
    private val backgroundDark = App.context.getColor(R.color.background_dark)


    val isLightTheme = Prefs.isLightTheme

    val color = ColorScope.fromColor(Prefs.color)

    val nextNearColor = ColorScope.fromColor(color.color.getNextNear())
    val prevNearColor = ColorScope.fromColor(color.color.getPrevNear())

    val nextSimilarColor = ColorScope.fromColor(color.color.getNextSimilar())
    val prevSimilarColor = ColorScope.fromColor(color.color.getPrevSimilar())

    val nextRangeColor = ColorScope.fromColor(color.color.getNextRange())
    val prevRangeColor = ColorScope.fromColor(color.color.getPrevRange())

    val nextTriColor = ColorScope.fromColor(color.color.getNextTri())
    val prevTriColor = ColorScope.fromColor(color.color.getPrevTri())

    val extraColor = ColorScope.fromColor(color.color.getExtra())

    val nextAnalogy = arrayOf(
            color,
            nextNearColor,
            nextSimilarColor,
            nextRangeColor
    )

    val prevAnalogy = arrayOf(
            color,
            prevNearColor,
            prevSimilarColor,
            prevRangeColor
    )

    val fullAnalogy = prevAnalogy.reversed().dropLast(1).plus(nextAnalogy).toTypedArray()

    val tricolor = arrayOf(
            color,
            nextTriColor,
            prevTriColor
    )

    val rectangle = arrayOf(
            color,
            nextRangeColor,
            extraColor,
            prevRangeColor
    )

    private fun Int.alphaOnWhite(alpha: Float = 0.5f) = mixWith(backgroundLight, alpha)
    private fun Int.alphaOnBlack(alpha: Float = 0.5f) = mixWith(backgroundDark, alpha)

    private fun Int.getNextNear() = toHsv().addHue(NEAR).toInt()
    private fun Int.getPrevNear() = toHsv().addHue(-NEAR).toInt()

    private fun Int.getNextSimilar() = toHsv().addHue(SIMILAR).toInt()
    private fun Int.getPrevSimilar() = toHsv().addHue(-SIMILAR).toInt()

    private fun Int.getNextRange() = toHsv().addHue(RANGE).toInt()
    private fun Int.getPrevRange() = toHsv().addHue(-RANGE).toInt()

    private fun Int.getNextTri() = toHsv().addHue(TRI).toInt()
    private fun Int.getPrevTri() = toHsv().addHue(-TRI).toInt()

    private fun Int.getExtra() = toHsv().addHue(EXTRA).toInt()

    private fun Int.mixWith(color: Int, alpha: Float = 0.5f): Int {
        val rgb = toRgb()
        val colorRgb = color.toRgb()
        val mixedRgb = Rgb(
                r = (rgb.r * alpha + colorRgb.r * (1 - alpha)).toInt(),
                g = (rgb.g * alpha + colorRgb.g * (1 - alpha)).toInt(),
                b = (rgb.b * alpha + colorRgb.b * (1 - alpha)).toInt()
        )
        return mixedRgb.toInt()
    }

    data class ColorScope(
            val color: Int,
            val colorWhite10: Int,
            val colorWhite20: Int,
            val colorWhite50: Int,
            val colorWhite75: Int,
            val colorBlack10: Int,
            val colorBlack20: Int,
            val colorBlack50: Int,
            val colorBlack75: Int
    ) {

        val color10: Int
            get() = if (isLightTheme) colorWhite10 else colorBlack10

        val color20: Int
            get() = if (isLightTheme) colorWhite20 else colorBlack20

        val color50: Int
            get() = if (isLightTheme) colorWhite50 else colorBlack50

        val color75: Int
            get() = if (isLightTheme) colorWhite75 else colorBlack75

        fun toList() = listOf(
                colorWhite10, colorWhite20, colorWhite50, colorWhite75,
                color, colorBlack75, colorBlack50, colorBlack20, colorBlack10
        )

        companion object {
            fun fromColor(color: Int): ColorScope = ColorScope(
                    color = color,
                    colorWhite10 = color.alphaOnWhite(0.1f),
                    colorWhite20 = color.alphaOnWhite(0.2f),
                    colorWhite50 = color.alphaOnWhite(0.5f),
                    colorWhite75 = color.alphaOnWhite(0.75f),
                    colorBlack10 = color.alphaOnBlack(0.1f),
                    colorBlack20 = color.alphaOnBlack(0.2f),
                    colorBlack50 = color.alphaOnBlack(0.5f),
                    colorBlack75 = color.alphaOnBlack(0.75f)
            )
        }
    }

}
