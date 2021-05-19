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

package com.twoeightnine.root.xvii.uikit

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.*
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twoeightnine.root.xvii.R
import kotlin.random.Random


fun Array<Munch.ColorScope>.randomFor(any: Any) =
        random(Random(any.hashCode()))

fun Drawable.paint(color: Int) {
    when (this) {
        is ShapeDrawable -> paint.color = color
        is GradientDrawable -> setColor(color)
        is ColorDrawable -> this.color = color
        is VectorDrawable -> setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}

fun Menu.paint(color: Int) {
    for (i in 0 until size()) {
        getItem(i).icon?.paint(color)
    }
}

fun View.paint(color: Int) {
    setBackgroundColor(color)
}

fun ImageView.paint(color: Int) {
    drawable.paint(color)
}

fun TextView.paint(color: Int) {
    setTextColor(color)
}

fun CheckBox.paint(color: Int) {
    buttonTintList = ColorStateList(
            arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
            ),
            intArrayOf(
                    color,
                    ContextCompat.getColor(context, R.color.minor_text)
            )
    )
}

fun SwitchCompat.paint(color: Munch.ColorScope) {
    val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
    )
    val thumbColors = intArrayOf(color.color, ContextCompat.getColor(context, R.color.switch_thumb_disabled))
    val trackColors = intArrayOf(color.color(20), ContextCompat.getColor(context, R.color.switch_track_disabled))

    DrawableCompat.setTintList(thumbDrawable, ColorStateList(states, thumbColors))
    DrawableCompat.setTintList(trackDrawable, ColorStateList(states, trackColors))
}

fun RadioButton.paint(color: Int) {
    val colorStateList = ColorStateList(
            arrayOf(
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf(android.R.attr.state_enabled)
            ),
            intArrayOf(
                    ContextCompat.getColor(context, R.color.minor_text),
                    color
            )
    )
    buttonTintList = colorStateList
}

fun BottomNavigationView.paint(color: Int) {

    val states = arrayOf(
            intArrayOf(android.R.attr.state_selected, android.R.attr.state_checked),
            intArrayOf()
    )

    val gray = ContextCompat.getColor(context, R.color.bottom_nav_non_active)
    val colors = intArrayOf(color, gray)
    itemIconTintList = ColorStateList(states, colors)
}