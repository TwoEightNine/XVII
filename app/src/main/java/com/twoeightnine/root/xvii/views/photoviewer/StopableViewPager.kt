package com.twoeightnine.root.xvii.views.photoviewer

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

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