package com.twoeightnine.root.xvii.uikit

import android.graphics.PorterDuff
import android.graphics.drawable.*


fun Drawable.paint(color: Int) {
    when (this) {
        is ShapeDrawable -> paint.color = color
        is GradientDrawable -> setColor(color)
        is ColorDrawable -> this.color = color
        is VectorDrawable -> setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}