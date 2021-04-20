package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton

class XviiFab(context: Context, attributeSet: AttributeSet) : FloatingActionButton(context, attributeSet) {

    init {
        backgroundTintList = ColorStateList.valueOf(Munch.color.color)
    }

}