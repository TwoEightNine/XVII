package com.twoeightnine.root.xvii.utils

import android.graphics.*
import com.squareup.picasso.Transformation
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R

class RoundedTransformation : Transformation {

    private val roundPx = App.context
            .resources
            .getDimensionPixelSize(R.dimen.image_corners_radius)
            .toFloat()

    override fun key() = "rounded"

    override fun transform(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        bitmap.recycle()
        return output
    }
}