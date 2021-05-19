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

package com.twoeightnine.root.xvii.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.R
import global.msnthrp.xvii.uikit.extensions.hide
import kotlinx.android.synthetic.main.view_color_select.view.*


class XviiColorSelect(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    var color: Int = Color.WHITE
        set(value) {
            field = value
            ivColor.setBackgroundColor(value)
        }

    init {
        View.inflate(context, R.layout.view_color_select, this)

        val attrsArray = intArrayOf(
                android.R.attr.text, // 0
                android.R.attr.hint // 1
        )
        val ta = context.obtainStyledAttributes(attributeSet, attrsArray)
        val text = ta.getText(0)
        val hint = ta.getText(1)
        ta.recycle()

        tvTitle.text = text
        tvHint.text = hint
        if (hint.isNullOrEmpty()) {
            tvHint.hide()
        }

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)

    }
}