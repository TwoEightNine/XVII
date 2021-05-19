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

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R

class XviiLabel(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {

    private var paintBackground = true

    init {
        initAttributes(attributeSet)

        if (paintBackground) {
            setBackgroundColor(Munch.color.color(15))
        }
        setTextColor(ContextCompat.getColor(context, R.color.main_text))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        typeface = Wool.get(context, Wool.Font.BOLD)
        val paddingStart = when {
            gravity == Gravity.CENTER_HORIZONTAL -> 0
            paddingStart != 0 -> paddingStart
            else -> 32
        }
        setPadding(paddingStart, 20, 0, 20)
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiLabel, 0, 0)
        paintBackground = attrs.getBoolean(R.styleable.XviiLabel_paintBackground, true)
        attrs.recycle()
    }
}