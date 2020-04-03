package com.twoeightnine.root.xvii.utils

import android.view.View
import androidx.core.view.ViewCompat

fun View.setBottomInsetPadding(viewHeight: Int = 0) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        view.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
        if (viewHeight != 0) {
            view.layoutParams.apply {
                height = viewHeight + insets.systemWindowInsetBottom
                view.layoutParams = this
            }
        }
        insets
    }
}

fun View.setTopInsetPadding(viewHeight: Int = 0) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        view.setPadding(0, insets.systemWindowInsetTop, 0, 0)
        if (viewHeight != 0) {
            view.layoutParams.apply {
                height = viewHeight + insets.systemWindowInsetTop
                view.layoutParams = this
            }
        }
        insets
    }
}

fun View.setBottomInsetMargin(viewMargin: Int = 0) {

}