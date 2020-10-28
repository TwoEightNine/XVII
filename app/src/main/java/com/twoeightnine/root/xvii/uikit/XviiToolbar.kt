package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.utils.setTopInsetPadding
import com.twoeightnine.root.xvii.utils.setVisible
import kotlinx.android.synthetic.main.toolbar2.view.*
import kotlinx.android.synthetic.main.view_tabs.view.*

class XviiToolbar(context: Context, attributeSet: AttributeSet) : AppBarLayout(context, attributeSet) {

    var title: String? = null
        set(value) {
            field = value
            tvToolbarTitle?.text = value
        }

    var forChat: Boolean = false
        set(value) {
            field = value
            rlChat?.setVisible(value)
            tvToolbarTitle?.text = if (value) "" else title
        }

    private var hasBackArrow: Boolean = true
    private var withTabs: Boolean = false
    private var alwaysLifted: Boolean = false

    var isLifted: Boolean
        get() = elevation != 0f
        set(value) {
            elevation = if (value) 10f else 0f
        }

    init {
        initAttributes(attributeSet)
        isLiftOnScroll = !alwaysLifted
        inflate(context, R.layout.toolbar2, this)
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        toolbar.title = ""
        if (!forChat) {
            tvToolbarTitle.text = title
        }
        toolbar.overflowIcon?.paint(Munch.color.color)

        if (withTabs) {
            addTabs()
        }
        rlChat.setVisible(forChat)

        setTopInsetPadding(/*context.resources.getDimensionPixelSize(R.dimen.toolbar_height)*/)
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiToolbar, 0, 0)
        title = attrs.getString(R.styleable.XviiToolbar_title)
        hasBackArrow = attrs.getBoolean(R.styleable.XviiToolbar_backArrow, true)
        withTabs = attrs.getBoolean(R.styleable.XviiToolbar_withTabs, false)
        forChat = attrs.getBoolean(R.styleable.XviiToolbar_forChat, false)
        alwaysLifted = attrs.getBoolean(R.styleable.XviiToolbar_alwaysLifted, false)
        attrs.recycle()
    }

    private fun addTabs() {
        (View.inflate(context, R.layout.view_tabs, null) as TabLayout).apply {
            tabTextColors = ColorStateList(
                    arrayOf(
                            intArrayOf(android.R.attr.state_selected),
                            intArrayOf()
                    ),
                    intArrayOf(
                            Munch.color.color,
                            ContextCompat.getColor(context, R.color.minor_text)
                    )
            )
            setSelectedTabIndicatorColor(Munch.color.color)
            this@XviiToolbar.addView(this)
        }
    }

    fun setupWith(baseActivity: BaseActivity) {
        baseActivity.setSupportActionBar(toolbar)
        baseActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(hasBackArrow)
            val homeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_back)
            homeDrawable?.paint(Munch.color.color)
            setHomeAsUpIndicator(homeDrawable)
            setHomeButtonEnabled(true)
            setDisplayUseLogoEnabled(false)
        }
    }

    fun setupWith(viewPager: ViewPager) {
        tabs.setupWithViewPager(viewPager, true)
    }
}