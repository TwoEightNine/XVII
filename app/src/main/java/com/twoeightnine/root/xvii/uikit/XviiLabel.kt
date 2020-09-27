package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class XviiLabel : AppCompatTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        setBackgroundColor(Munch.color.color25)
    }
}