package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.graphics.*
import androidx.palette.graphics.Palette
import com.twoeightnine.root.xvii.lg.L
import global.msnthrp.xvii.uikit.utils.color.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File

object BitmapNotification {

    private const val TAG = "bitmap notification"
    private const val NEEDED_CONTRAST = 4.0
    private val LIGHT_LIGHTNESSES = listOf(0.6f, 0.7f, 0.8f, 0.9f, 0.95f)
    private val DARK_LIGHTNESSES = listOf(0.4f, 0.3f, 0.2f, 0.1f, 0.05f)

    fun load(
            context: Context, avatar: Bitmap,
            onLoaded: (NotificationBackground) -> Unit
    ): Disposable = Single.fromCallable { getOrCreateNotificationBackground(context, avatar) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onLoaded, { error ->
                L.tag(TAG).throwable(error)
                        .log("error while loading bitmap")
            })

    private fun getOrCreateNotificationBackground(context: Context, avatar: Bitmap): NotificationBackground {
        val hash = avatar.hash()
        val dir = File(context.cacheDir, DIR_NOTIFICATIONS)
        dir.mkdir()

        val file = File(dir, "${hash + 1}.png")
        return if (!file.exists()) {
            L.tag(TAG).log("creating")
            val start = System.currentTimeMillis()
            val notificationBackground = createNotificationBackground(avatar)
            saveBmp(file.absolutePath, notificationBackground.background)
            L.tag(TAG).log("took ${System.currentTimeMillis() - start} ms")
            notificationBackground
        } else {
            L.tag(TAG).log("opening")
            val start = System.currentTimeMillis()
            val imageColors = getImageColors(avatar)
            val textColor = getTextColor(imageColors)
            val notificationBackground = NotificationBackground(
                    BitmapFactory.decodeFile(file.absolutePath),
                    textColor,
                    imageColors.averageColor
            )
            L.tag(TAG).log("took ${System.currentTimeMillis() - start} ms")
            notificationBackground
        }
    }

    private fun createNotificationBackground(avatar: Bitmap, debug: Boolean = false): NotificationBackground {

        val backgroundWidth = 720
        val backgroundHeight = 180
        val background = Bitmap.createBitmap(backgroundWidth, backgroundHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(background)

        val imageColors = getImageColorsViaPalette(avatar)

        val avgHsv = FloatArray(3)
        Color.colorToHSV(imageColors.averageColor, avgHsv)
        var newS = avgHsv[1]
        newS = when {
            newS > 0.9f -> 1f
            else -> newS + 0.1f
        }
        avgHsv[1] = newS
        val averageColor = Color.HSVToColor(avgHsv)
        canvas.drawColor(averageColor)
        imageColors.averageColor = averageColor

        val avatarWidth = avatar.width
        val avatarHeight = avatar.height
        val avatarRect = Rect(0, 0, avatarWidth, avatarHeight)

        val backgroundRect = Rect(0, 0, backgroundHeight, backgroundHeight)
        canvas.drawBitmap(avatar, avatarRect, backgroundRect, null)

        val paint = Paint().apply { style = Paint.Style.FILL }
        for (i in 0 until backgroundHeight) {
            for (j in 0 until backgroundHeight) {
                val bias = i.toFloat() / backgroundHeight

                val pixColor = background.getPixel(i, j)
                if (pixColor.hasAlpha()) continue

                val pixR = (pixColor shr 16) and 0xff
                val pixG = (pixColor shr 8) and 0xff
                val pixB = (pixColor shr 0) and 0xff

                val avgR = (averageColor shr 16) and 0xff
                val avgG = (averageColor shr 8) and 0xff
                val avgB = (averageColor shr 0) and 0xff

                val newR = (pixR * (1 - bias) + avgR * bias).toInt() and 0xff
                val newG = (pixG * (1 - bias) + avgG * bias).toInt() and 0xff
                val newB = (pixB * (1 - bias) + avgB * bias).toInt() and 0xff

                val newColor = (0xff shl 24) or (newR shl 16) or (newG shl 8) or newB
                paint.color = newColor
                canvas.drawPoint(i.toFloat(), j.toFloat(), paint)
            }
        }

        val textColor = getTextColor(imageColors)
        if (debug) {
            paint.color = textColor
            canvas.drawRect(400f, 80f, 600f, 120f, paint)
        }
        return NotificationBackground(background, textColor, averageColor)
    }

    private fun getTextColor(imageColors: ImageColors): Int {

        val contrastWithLight = ColorUtils.getContrastRatio(imageColors.averageColor, imageColors.averageLight)
        val contrastWithDark = ColorUtils.getContrastRatio(imageColors.averageColor, imageColors.averageDark)
        val contrastWithBlack = ColorUtils.getContrastRatio(imageColors.averageColor, Color.BLACK)
        val contrastWithWhite = ColorUtils.getContrastRatio(imageColors.averageColor, Color.WHITE)

        return when {
            contrastWithLight > contrastWithDark
                    && contrastWithLight >= NEEDED_CONTRAST -> imageColors.averageLight

            contrastWithDark > contrastWithLight
                    && contrastWithDark >= NEEDED_CONTRAST -> imageColors.averageDark

            contrastWithWhite > contrastWithBlack -> getColorOfContrast(
                    imageColors.averageColor, imageColors.averageLight, LIGHT_LIGHTNESSES, NEEDED_CONTRAST, Color.WHITE
            )

            else -> getColorOfContrast(
                    imageColors.averageColor, imageColors.averageDark, DARK_LIGHTNESSES, NEEDED_CONTRAST, Color.BLACK
            )
        }
    }

    private fun getColorOfContrast(back: Int, colorFrom: Int, lightnessRange: List<Float>, contrast: Double, default: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(colorFrom, hsv)
        for (lightnessProb in lightnessRange) {

            hsv[2] = lightnessProb
            val newColor = Color.HSVToColor(hsv)
            val newContrast = ColorUtils.getContrastRatio(back, newColor)

            if (newContrast >= contrast) {
                return newColor
            }
        }
        return default
    }

    private fun getImageColorsViaPalette(bitmap: Bitmap): ImageColors {
        val palette = Palette.Builder(bitmap).generate()
        val dominant = palette.getDominantColor(Color.BLACK)
        val darkMuted = palette.getDarkMutedColor(Color.BLACK)
        val lightVibrant = palette.getLightVibrantColor(Color.WHITE)
        return ImageColors(dominant, darkMuted, lightVibrant)
    }

    /**
     * returns:
     *      - average color of the right part of [bitmap]
     *      - average dark for the whole image
     *      - average light for the whole image
     */
    private fun getImageColors(bitmap: Bitmap): ImageColors {

        val avgThreshold = bitmap.width * 9 / 10

        var avgColorR = 0
        var avgColorG = 0
        var avgColorB = 0
        var avgColorsCount = 0

        var darkAvgR = 0
        var darkAvgG = 0
        var darkAvgB = 0
        var darkColorsCount = 0

        var lightAvgR = 0
        var lightAvgG = 0
        var lightAvgB = 0
        var lightColorsCount = 0

        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(i, j)
                if (pixel.hasAlpha()) continue


                val r = pixel.red()
                val g = pixel.green()
                val b = pixel.blue()

                if (i >= avgThreshold) {
                    val avgCoeff = bitmap.width - i - avgThreshold
                    avgColorR += r * avgCoeff
                    avgColorG += g * avgCoeff
                    avgColorB += b * avgCoeff
                    avgColorsCount += avgCoeff
                }

                val brightness = pixel.brightness()
                when {

                    brightness < 128 -> {
                        darkAvgR += r
                        darkAvgG += g
                        darkAvgB += b
                        darkColorsCount++
                    }
                    else -> {
                        lightAvgR += r
                        lightAvgG += g
                        lightAvgB += b
                        lightColorsCount++
                    }

                }
            }

        }

        if (avgColorsCount != 0) {
            avgColorR /= avgColorsCount
            avgColorG /= avgColorsCount
            avgColorB /= avgColorsCount
        }

        if (darkColorsCount != 0) {
            darkAvgR /= darkColorsCount
            darkAvgG /= darkColorsCount
            darkAvgB /= darkColorsCount
        } // else dark will be 0,0,0 i.e. black

        if (lightColorsCount != 0) {
            lightAvgR /= lightColorsCount
            lightAvgG /= lightColorsCount
            lightAvgB /= lightColorsCount
        } else {
            lightAvgR = 255
            lightAvgG = 255
            lightAvgB = 255
        }
        return ImageColors(
                Color.rgb(avgColorR, avgColorG, avgColorB),
                Color.rgb(darkAvgR, darkAvgG, darkAvgB),
                Color.rgb(lightAvgR, lightAvgG, lightAvgB)
        )
    }

    private fun Bitmap.hash(): Int {
        var hash = 31
        (0 until width).step(7).forEach { x ->
            (0 until height).step(6).forEach { y ->
                hash = (hash * 31 + getPixel(x, y)).rem(Int.MAX_VALUE) // it is prime
            }
        }
        return hash
    }

    private data class ImageColors(
            var averageColor: Int,
            val averageDark: Int,
            val averageLight: Int
    )

    data class NotificationBackground(
            val background: Bitmap,
            val textColor: Int,
            val backgroundColor: Int
    )
}
