package global.msnthrp.xvii.uikit.utils.color

import android.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

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