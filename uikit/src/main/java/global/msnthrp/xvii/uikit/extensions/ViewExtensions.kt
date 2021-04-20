package global.msnthrp.xvii.uikit.extensions

import android.view.View
import android.view.ViewTreeObserver


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

inline fun View.onReady(crossinline callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            callback()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

inline fun View.onPreDraw(crossinline callback: () -> Unit) {
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            callback()
            viewTreeObserver.removeOnPreDrawListener(this)
            return true
        }
    })
}
