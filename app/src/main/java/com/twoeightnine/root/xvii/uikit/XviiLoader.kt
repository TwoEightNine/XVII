package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.view_loader.view.*

class XviiLoader(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    private val extraPadding by lazy {
        context.resources.getDimensionPixelSize(R.dimen.loader_extra_padding)
    }

    private var alwaysWhite = false
    private var size = Size.USUAL

    init {
        initAttributes(attributeSet)
        View.inflate(context, R.layout.view_loader, this)
        circularProgress.apply {
            if (alwaysWhite) {
                setIndicatorColor(Color.WHITE)
            } else {
                setIndicatorColor(*Munch.nextAnalogy)
            }
            indicatorSize = size.getIndicatorSize(context)
            trackThickness = size.getTrackThickness(context)
            trackCornerRadius = trackThickness / 2
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = this.size.getIndicatorSize(context) + extraPadding
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

    enum class Size(
            private val indicatorSizeDimenRes: Int,
            private val trackThicknessDimenRes: Int
    ) {
        USUAL(R.dimen.loader_usual_size, R.dimen.loader_usual_thickness),
        SMALL(R.dimen.loader_small_size, R.dimen.loader_small_thickness);

        fun getIndicatorSize(context: Context) =
                context.resources.getDimensionPixelSize(indicatorSizeDimenRes)

        fun getTrackThickness(context: Context) =
                context.resources.getDimensionPixelSize(trackThicknessDimenRes)
    }
}