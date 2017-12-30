package com.twoeightnine.root.xvii.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.RelativeLayout

class ReplierHelper(var item: RelativeLayout,
                    var listener: (() -> Unit)?,
                    var widthPx: Int) {

    val duration = 300L

    init {
        item.visibility = View.GONE
        (item.layoutParams as FrameLayout.LayoutParams).rightMargin = -widthPx
        item.setOnClickListener({ listener?.invoke() })
    }

    fun openItem() {
        if (!isOpen()) {
            val params = item.layoutParams as FrameLayout.LayoutParams
            params.rightMargin = -(widthPx)
            item.visibility = View.VISIBLE
            val animator = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    super.applyTransformation(interpolatedTime, t)
                    params.rightMargin =
                            -((widthPx) * (1 - interpolatedTime)).toInt()
                    item.layoutParams = params
                }
            }
            animator.duration = duration
            item.startAnimation(animator)
        }
    }

    fun closeItem() {
        if (isOpen()) {
            val params = item.layoutParams as FrameLayout.LayoutParams
            val animator = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    super.applyTransformation(interpolatedTime, t)
                    params.rightMargin =
                            -((widthPx) * interpolatedTime).toInt()
                    item.layoutParams = params
                }
            }
            animator.duration = duration
            animator.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    item.visibility = View.GONE
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
            item.startAnimation(animator)
        }
    }

    fun isOpen() = item.visibility == View.VISIBLE

}