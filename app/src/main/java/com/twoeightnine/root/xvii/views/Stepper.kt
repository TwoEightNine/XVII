package com.twoeightnine.root.xvii.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.ColorManager
import com.twoeightnine.root.xvii.utils.stylizeAnyway
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
        ivMinus.stylizeAnyway(ColorManager.MAIN_TAG)
        ivPlus.stylizeAnyway(ColorManager.MAIN_TAG)
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