package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.view_loader.view.*

class XviiLoader(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    private var alwaysWhite = false

    init {
        initAttributes(attributeSet)
        View.inflate(context, R.layout.view_loader, this)
        circularProgress.apply {
            if (alwaysWhite) {
                setIndicatorColor(Color.WHITE)
            } else {
                setIndicatorColor(*Munch.nextAnalogy.map { it.color }.toIntArray())
            }
        }
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiLoader, 0, 0)
        alwaysWhite = attrs.getBoolean(R.styleable.XviiLoader_alwaysWhite, false)
        attrs.recycle()
    }
}