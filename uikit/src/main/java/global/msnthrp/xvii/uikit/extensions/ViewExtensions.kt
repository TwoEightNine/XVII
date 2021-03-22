package global.msnthrp.xvii.uikit.extensions

import android.view.View


fun View.setVisible(visible: Boolean) {
    visibility = when {
        visible -> View.VISIBLE
        else -> View.GONE
    }
}

fun View.setVisibleWithInvis(visible: Boolean) {
    visibility = when {
        visible -> View.VISIBLE
        else -> View.INVISIBLE
    }
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.toggle() {
    setVisible(!isVisible())
}

fun View.show() = setVisible(true)

fun View.hide() = setVisible(false)

fun View.hideInvis() = setVisibleWithInvis(false)
