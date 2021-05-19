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
