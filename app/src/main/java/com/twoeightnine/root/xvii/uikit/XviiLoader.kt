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
    private var size = Size.USUAL

    init {
        initAttributes(attributeSet)
        View.inflate(context, R.layout.view_loader, this)
        circularProgress.apply {
            if (alwaysWhite) {
                setIndicatorColor(Color.WHITE)
            } else {
                setIndicatorColor(*Munch.nextAnalogy.map { it.color }.toIntArray())
            }
            indicatorSize = size.indicatorSize
            trackThickness = size.trackThickness
            trackCornerRadius = size.trackThickness / 2
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = this.size.indicatorSize + EXTRA_PADDING
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        )
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiLoader, 0, 0)
        alwaysWhite = attrs.getBoolean(R.styleable.XviiLoader_alwaysWhite, false)
        size = Size.values()[attrs.getInt(R.styleable.XviiLoader_size, 0)]
        attrs.recycle()
    }

    companion object {
        private const val EXTRA_PADDING = 14
    }

    enum class Size(val indicatorSize: Int, val trackThickness: Int) {
        USUAL(80, 8),
        SMALL(48, 6)
    }
}