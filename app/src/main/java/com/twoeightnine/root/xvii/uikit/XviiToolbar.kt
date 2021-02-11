package com.twoeightnine.root.xvii.uikit

import android.animation.ValueAnimator
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
import global.msnthrp.xvii.uikit.extensions.*
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

    var showLogo: Boolean = false
        set(value) {
            field = value
            setLogoVisible(value)
        }

    private var hasBackArrow: Boolean = true
    private var withTabs: Boolean = false
    private var alwaysLifted: Boolean = false


    private var animationRunning = false

//    var isLifted: Boolean
//        get() = elevation != 0f
//        set(value) {
//            updateElevation(if (value) 10f else 0f)
//        }

    var onClick: (() -> Unit)? = null
        set(value) {
            field = value
            toolbar?.setOnClickListener { value?.invoke() }
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
        setLogoVisible(showLogo)

        applyTopInsetPadding()
    }

    private fun initAttributes(attributeSet: AttributeSet) {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiToolbar, 0, 0)
        title = attrs.getString(R.styleable.XviiToolbar_title)
        hasBackArrow = attrs.getBoolean(R.styleable.XviiToolbar_backArrow, true)
        withTabs = attrs.getBoolean(R.styleable.XviiToolbar_withTabs, false)
        forChat = attrs.getBoolean(R.styleable.XviiToolbar_forChat, false)
        alwaysLifted = attrs.getBoolean(R.styleable.XviiToolbar_alwaysLifted, false)
        showLogo = attrs.getBoolean(R.styleable.XviiToolbar_showLogo, false)
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

    private fun updateElevation(newElevation: Float) = synchronized(this) {
        val currentElevation = elevation
        if (animationRunning || newElevation == currentElevation) return@synchronized

        ValueAnimator.ofFloat(currentElevation, newElevation).apply {
            duration = LIFT_ANIM_DURATION
            addUpdateListener { animation ->
                elevation = animation.animatedValue as Float
            }
            addListener(EndAnimatorListener {
                animationRunning = false
            })
            start()
            animationRunning = true
        }
    }

    private fun setLogoVisible(visible: Boolean) {
        ivToolbarLogo?.setVisible(visible)
        tvToolbarTitle?.text = if (visible) "" else title
        if (visible) {
            ivToolbarLogo?.paint(ContextCompat.getColor(context, R.color.main_text))
        }
    }

    companion object {
        private const val LIFT_ANIM_DURATION = 120L
    }
}