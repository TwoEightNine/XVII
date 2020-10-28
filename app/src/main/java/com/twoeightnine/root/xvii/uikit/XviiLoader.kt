package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ProgressBar
import com.twoeightnine.root.xvii.R

class XviiLoader(context: Context, attributeSet: AttributeSet) : ProgressBar(context, attributeSet) {

    private var alwaysWhite = false

    init {
        initAttributes(attributeSet)
        indeterminateTintList = ColorStateList.valueOf(if (alwaysWhite) {
            Color.WHITE
        } else {
            Munch.color.color
        })
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiLoader, 0, 0)
        alwaysWhite = attrs.getBoolean(R.styleable.XviiLoader_alwaysWhite, false)
        attrs.recycle()
    }
}