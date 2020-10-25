package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.view_item.view.*

class XviiItem(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    private var title: String? = null
    private var icon: Drawable? = null

    init {
        View.inflate(context, R.layout.view_item, this)
        setBackgroundResource(R.drawable.selector_rect)
        initAttributes(attributeSet)

        tvTitle.text = title
        icon?.apply {
            paint(Munch.color.color)
            ivIcon.setImageDrawable(this)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(96, MeasureSpec.EXACTLY))
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiItem, 0, 0)
        title = attrs.getString(R.styleable.XviiItem_itemTitle)
        icon = attrs.getDrawable(R.styleable.XviiItem_itemIcon)
        attrs.recycle()
    }
}