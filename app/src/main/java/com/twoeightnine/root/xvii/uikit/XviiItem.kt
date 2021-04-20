package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.view_item.view.*

class XviiItem(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    private val itemHeight by lazy {
        context.resources.getDimensionPixelSize(R.dimen.item_height)
    }

    private var title: String? = null
    private var icon: Drawable? = null
//    private var hasDivider: Boolean = true

    init {
        View.inflate(context, R.layout.view_item, this)
        setBackgroundResource(R.drawable.selector_rect)
        initAttributes(attributeSet)

        tvTitle.text = title
        icon?.apply {
            paint(Munch.color.color)
            ivIcon.setImageDrawable(this)
        }
//        vItemDivider.setVisible(hasDivider)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY))
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiItem, 0, 0)
        title = attrs.getString(R.styleable.XviiItem_itemTitle)
        icon = attrs.getDrawable(R.styleable.XviiItem_itemIcon)
//        hasDivider = attrs.getBoolean(R.styleable.XviiItem_hasDivider, hasDivider)
        attrs.recycle()
    }
}