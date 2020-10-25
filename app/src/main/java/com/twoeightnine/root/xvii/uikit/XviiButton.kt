package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R

class XviiButton : AppCompatButton {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.shape_button)
        backgroundDrawable?.paint(Munch.color.color)
        background = backgroundDrawable
        setTextColor(Color.WHITE)

        stateListAnimator = null
        elevation = 1f
        setPadding(24, 0, 24, 0)
        compoundDrawablePadding = 16
        transformationMethod = null
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        typeface = Typeface.createFromAsset(context.resources.assets, "fonts/medium.ttf")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modifiedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(88, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, modifiedHeightMeasureSpec)
    }
}