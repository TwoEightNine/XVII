package com.twoeightnine.root.xvii.uikit

import android.graphics.Color

fun Rgb.toHsv(): Hsv = Hsv.fromInt(toInt())

fun Hsv.toRgb(): Rgb = Rgb.fromInt(toInt())

fun Int.toRgb(): Rgb = Rgb.fromInt(this)

fun Int.toHsv(): Hsv = Hsv.fromInt(this)

fun Hsv.addHue(extra: Int) = copy(h = (h + extra) % 360)

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