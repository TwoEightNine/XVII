package global.msnthrp.xvii.uikit.extensions

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

fun View.applyBottomInsetPadding() =
        doOnApplyWindowInsets { view, insets, padding, _ ->
            view.updatePadding(
                    bottom = padding.bottom + insets.systemWindowInsetBottom
            )
            insets
        }

fun View.applyTopInsetPadding() =
        doOnApplyWindowInsets { view, insets, padding, _ ->
            view.updatePadding(
                    top = padding.top + insets.systemWindowInsetTop
            )
            insets
        }

fun View.applyBottomInsetMargin() =
        doOnApplyWindowInsets { view, insets, _, margin ->
            (view.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                updateMargins(
                        bottom = margin.bottom + insets.systemWindowInsetBottom
                )
            }
            insets
        }

fun View.applyTopInsetMargin() =
        doOnApplyWindowInsets { view, insets, _, margin ->
            (view.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                updateMargins(
                        top = margin.top + insets.systemWindowInsetTop
                )
            }
            insets
        }

fun View.doOnApplyWindowInsets(block: (View, WindowInsetsCompat, Rect, Rect) -> WindowInsetsCompat) {
    val initialPadding = recordInitialPadding()
    val initialMargin = recordInitialMargin()
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        block(v, insets, initialPadding, initialMargin)
    }
    requestApplyInsetsWhenAttached()
}

private fun View.recordInitialPadding() =
        Rect(paddingLeft, paddingTop, paddingRight, paddingBottom)


private fun View.recordInitialMargin() =
        (layoutParams as? ViewGroup.MarginLayoutParams)
                ?.let { Rect(marginLeft, marginTop, marginRight, marginBottom) }
                ?: Rect(0, 0, 0, 0)


private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}