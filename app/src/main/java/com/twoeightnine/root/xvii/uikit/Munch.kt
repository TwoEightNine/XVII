package com.twoeightnine.root.xvii.uikit

import android.graphics.Color
import com.twoeightnine.root.xvii.managers.Prefs

object Munch {

    private const val NEAR = 30
    private const val SIMILAR = 60
    private const val RANGE = 90
    private const val TRI = 120
    private const val EXTRA = 180

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

    val tricolor = arrayOf(
            color,
            nextTriColor,
            prevTriColor
    )

    private fun Int.alphaOnWhite(alpha: Float = 0.5f) = mixWith(Color.WHITE, alpha)
    private fun Int.alphaOnBlack(alpha: Float = 0.5f) = mixWith(Color.BLACK, alpha)

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
            val colorWhite25: Int,
            val colorWhite50: Int,
            val colorWhite75: Int,
            val colorBlack10: Int,
            val colorBlack25: Int,
            val colorBlack50: Int,
            val colorBlack75: Int
    ) {

        val color10: Int
            get() = if (isLightTheme) colorWhite10 else colorBlack10

        val color25: Int
            get() = if (isLightTheme) colorWhite25 else colorBlack25

        val color50: Int
            get() = if (isLightTheme) colorWhite50 else colorBlack50

        val color75: Int
            get() = if (isLightTheme) colorWhite75 else colorBlack75

        fun toList() = listOf(
                colorWhite10, colorWhite25, colorWhite50, colorWhite75,
                color, colorBlack75, colorBlack50, colorBlack25, colorBlack10
        )

        companion object {
            fun fromColor(color: Int): ColorScope = ColorScope(
                    color = color,
                    colorWhite10 = color.alphaOnWhite(0.1f),
                    colorWhite25 = color.alphaOnWhite(0.25f),
                    colorWhite50 = color.alphaOnWhite(0.5f),
                    colorWhite75 = color.alphaOnWhite(0.75f),
                    colorBlack10 = color.alphaOnBlack(0.1f),
                    colorBlack25 = color.alphaOnBlack(0.25f),
                    colorBlack50 = color.alphaOnBlack(0.5f),
                    colorBlack75 = color.alphaOnBlack(0.75f)
            )
        }
    }

}
