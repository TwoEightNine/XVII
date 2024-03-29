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

package global.msnthrp.xvii.uikit.utils.color

import android.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ColorUtils {

    fun isColorBetterWithWhite(color: Int): Boolean {
        val contrastWithWhite = getContrastRatio(color, Color.WHITE)
        val contrastWithBlack = getContrastRatio(color, Color.BLACK)

        return contrastWithWhite > contrastWithBlack
    }


    fun getContrastRatio(c1: Int, c2: Int): Double {
        val rl1 = c1.relativeLuminance()
        val rl2 = c2.relativeLuminance()

        return if (rl1 > rl2) {
            (rl1 + 0.05) / (rl2 + 0.05)
        } else {
            (rl2 + 0.05) / (rl1 + 0.05)
        }
    }

    private fun Int.relativeLuminance(): Float {
        val rs = red().toFloat() / 255
        val gs = green().toFloat() / 255
        val bs = blue().toFloat() / 255

        val r = if (rs <= 0.03928) {
            rs / 12.92
        } else {
            ((rs + 0.055) / 1.055).pow(2.4)
        }
        val g = if (gs <= 0.03928) {
            gs / 12.92
        } else {
            ((gs + 0.055) / 1.055).pow(2.4)
        }
        val b = if (bs <= 0.03928) {
            bs / 12.92
        } else {
            ((bs + 0.055) / 1.055).pow(2.4)
        }
        return (0.2126 * r + 0.7152 * g + 0.0722 * b).toFloat()
    }
}

fun Rgb.toHsv(): Hsv = Hsv.fromInt(toInt())

fun Hsv.toRgb(): Rgb = Rgb.fromInt(toInt())

fun Int.toRgb(): Rgb = Rgb.fromInt(this)

fun Int.toHsv(): Hsv = Hsv.fromInt(this)

fun Hsv.addHue(extra: Int) = copy(h = (h + extra) % 360)

fun Int.hasAlpha() = this and 0xff000000.toInt() != 0xff000000.toInt()

fun Int.red() = (this shr 16) and 0xff

fun Int.green() = (this shr 8) and 0xff

fun Int.blue() = this and 0xff

fun Int.brightness(): Int {
    val r = red()
    val g = green()
    val b = blue()
    return (max(r, max(g, b)) + min(r, min(g, b))) / 2
}

data class Rgb(
        val r: Int,
        val g: Int,
        val b: Int
) {
    fun toInt(): Int =
            0xff000000.toInt() or
                    ((r and 0xff) shl 16) or
                    ((g and 0xff) shl 8) or
                    (b and 0xff)

    companion object {

        fun fromInt(color: Int) = Rgb(
                r = (color shr 16) and 0xff,
                g = (color shr 8) and 0xff,
                b = color and 0xff
        )
    }
}

data class Hsv(
        val h: Int,
        val s: Float,
        val v: Float
) {

    fun toInt(): Int {
        val hsv = floatArrayOf(h.toFloat(), s, v)
        return Color.HSVToColor(hsv)
    }

    companion object {

        fun fromInt(color: Int): Hsv {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            return Hsv(
                    h = hsv[0].toInt(),
                    s = hsv[1],
                    v = hsv[2]
            )
        }
    }
}