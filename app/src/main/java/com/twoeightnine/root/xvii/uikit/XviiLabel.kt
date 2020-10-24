package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R

class XviiLabel : AppCompatTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        setBackgroundColor(Munch.color.color25)
        setTextColor(ContextCompat.getColor(context, R.color.main_text))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        setPadding(32, 20, 0, 20)
    }
}