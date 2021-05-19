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

package global.msnthrp.xvii.uikit.extensions

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

fun View.applyHorizontalInsetPadding() =
        doOnApplyWindowInsets { view, insets, padding, _ ->
            view.updatePadding(
                    bottom = padding.bottom + insets.systemWindowInsetBottom,
                    top = padding.top + insets.systemWindowInsetTop
            )
            insets
        }

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