package com.twoeightnine.root.xvii.views

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.RelativeLayout

class InsettableRelativeLayout : RelativeLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets? {
        val childCount = childCount
        for (index in 0 until childCount) {
            getChildAt(index).dispatchApplyWindowInsets(insets)
        }
        return insets
    }
}