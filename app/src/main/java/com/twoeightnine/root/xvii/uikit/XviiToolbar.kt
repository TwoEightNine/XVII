package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.utils.ColorManager
import com.twoeightnine.root.xvii.utils.setTopInsetPadding
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.toolbar2.view.*

class XviiToolbar(context: Context, attributeSet: AttributeSet) : AppBarLayout(context, attributeSet) {

    private var title: String? = null

    private var hasBackArrow: Boolean = true

    init {
        initAttributes(attributeSet)
        isLiftOnScroll = true
        inflate(context, R.layout.toolbar2, this)
        toolbar.title = ""
        tvTitle.text = title
        setTopInsetPadding(context.resources.getDimensionPixelSize(R.dimen.toolbar_height))
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiToolbar, 0, 0)
        title = attrs.getString(R.styleable.XviiToolbar_title)
        hasBackArrow = attrs.getBoolean(R.styleable.XviiToolbar_backArrow, true)
        attrs.recycle()
    }

    fun setupWith(baseActivity: BaseActivity) {
        baseActivity.setSupportActionBar(toolbar)
        baseActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(hasBackArrow)
            val homeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_back)
            homeDrawable?.stylize(ColorManager.MAIN_TAG)
            setHomeAsUpIndicator(homeDrawable)
            setHomeButtonEnabled(true)
            setDisplayUseLogoEnabled(false)
        }
    }
}