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
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R

class XviiButton(context: Context, attributeSet: AttributeSet) : AppCompatButton(context, attributeSet) {

    var warn: Boolean = false
        private set

    init {
        initAttributes(attributeSet)

        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.shape_button)
        backgroundDrawable?.paint(if (warn) {
            ContextCompat.getColor(context, R.color.popup_error)
        } else {
            Munch.color.color
        })
        background = backgroundDrawable
        setTextColor(Color.WHITE)

        stateListAnimator = null
        elevation = 1f
        setPadding(24, 0, 24, 0)
        compoundDrawablePadding = 16
        transformationMethod = null
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        typeface = Wool.get(context, Wool.Font.MEDIUM)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modifiedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(88, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, modifiedHeightMeasureSpec)
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiButton, 0, 0)
        warn = attrs.getBoolean(R.styleable.XviiButton_warn, false)
        attrs.recycle()
    }
}