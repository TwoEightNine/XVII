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
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import kotlinx.android.synthetic.main.view_stepper.view.*

class Stepper(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    private val tvValue: TextView
    private val tvName: TextView
    private val tvHint: TextView

    /**
     * inclusive end
     */
    var max: Int = MAX_DEFAULT
        set(value) {
            if (max < min) {
                throw IllegalArgumentException("Max (given $value) must be more than min ($min)")
            }
            field = value
        }

    /**
     * inclusive start
     */
    var min: Int = MIN_DEFAULT
        set(value) {
            if (min > max) {
                throw IllegalArgumentException("Min (given $value) must be less than max ($max)")
            }
            field = value
        }

    var step: Int = STEP_DEFAULT
        set(value) {
            if (step <= 0) {
                throw IllegalArgumentException("Step must be positive integer! Given: $value")
            }
            field = value
        }

    var value: Int = VALUE_DEFAULT
        set(value) {
            if (value !in IntRange(min, max)) {
                throw IllegalArgumentException("Value must be in range [$min, $max]! Given: $value")
            }
            field = value
            invalidateValue()
        }

    var text: CharSequence
        get() = tvName.text
        set(value) {
            tvName.text = value
        }

    var onValueChangedListener: ((Int) -> Unit)? = null

//    constructor(context: Context) : super(context)

    init {
        val view = View.inflate(context, R.layout.view_stepper, null)
        addView(view)
        with(view) {
            ivPlus.setOnClickListener {
                if (value + step <= max) {
                    value += step
                }
            }
            ivMinus.setOnClickListener {
                if (value - step >= min) {
                    value -= step
                }
            }
            this@Stepper.tvValue = tvValue
            this@Stepper.tvName = tvName
            this@Stepper.tvHint = tvHint
        }
        initAttributes(attributeSet)
        invalidateValue()
        ivMinus.paint(Munch.color.color)
        ivPlus.paint(Munch.color.color)
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.Stepper, 0, 0)
        tvName.text = attrs.getString(R.styleable.Stepper_text)
        tvHint.text = attrs.getString(R.styleable.Stepper_hint)
        max = attrs.getInt(R.styleable.Stepper_max, MAX_DEFAULT)
        min = attrs.getInt(R.styleable.Stepper_min, MIN_DEFAULT)
        step = attrs.getInt(R.styleable.Stepper_step, STEP_DEFAULT)
        value = attrs.getInt(R.styleable.Stepper_value, min)
        attrs.recycle()
    }

    private fun invalidateValue() {
        tvValue.text = "$value"
        onValueChangedListener?.invoke(value)
    }

    companion object {
        const val MAX_DEFAULT = 10
        const val MIN_DEFAULT = 0
        const val VALUE_DEFAULT = 0
        const val STEP_DEFAULT = 1
    }
}