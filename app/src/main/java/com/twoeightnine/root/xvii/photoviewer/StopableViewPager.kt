package com.twoeightnine.root.xvii.photoviewer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Created by twoeightnine on 1/25/18.
 */
class StopableViewPager : ViewPager {

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context) : super(context)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val canSlide = if (adapter is FullScreenImageAdapter) {
            (adapter as FullScreenImageAdapter).canSlide(currentItem) ?: false
        } else {
            false
        }
        return !canSlide && super.onInterceptTouchEvent(ev)
    }
}