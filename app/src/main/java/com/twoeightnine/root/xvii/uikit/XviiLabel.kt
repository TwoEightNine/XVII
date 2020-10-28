package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R

class XviiLabel : AppCompatTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        setBackgroundColor(Munch.color.color20)
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
}