package com.twoeightnine.root.xvii.uikit

import android.graphics.Color
import com.twoeightnine.root.xvii.managers.Prefs

object Munch {

    private const val NEAR = 30
    private const val SIMILAR = 60
    private const val RANGE = 90
    private const val TRI = 120
    private const val EXTRA = 180

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
            val colorWhite05: Int,
            val colorWhite10: Int,
            val colorWhite30: Int,
            val colorWhite50: Int,
            val colorWhite70: Int,
            val colorBlack30: Int,
            val colorBlack50: Int,
            val colorBlack70: Int,
            val colorBlack90: Int
    ) {

        fun toList() = listOf(
                colorWhite05, colorWhite10, colorWhite30, colorWhite50, colorWhite70,
                color, colorBlack90, colorBlack70, colorBlack50, colorBlack30
        )

        companion object {
            fun fromColor(color: Int): ColorScope = ColorScope(
                    color = color,
                    colorWhite05 = color.alphaOnWhite(0.05f),
                    colorWhite10 = color.alphaOnWhite(0.1f),
                    colorWhite30 = color.alphaOnWhite(0.3f),
                    colorWhite50 = color.alphaOnWhite(0.5f),
                    colorWhite70 = color.alphaOnWhite(0.7f),
                    colorBlack30 = color.alphaOnBlack(0.3f),
                    colorBlack50 = color.alphaOnBlack(0.5f),
                    colorBlack70 = color.alphaOnBlack(0.7f),
                    colorBlack90 = color.alphaOnBlack(0.9f)
            )
        }
    }

}
