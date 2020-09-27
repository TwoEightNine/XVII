package com.twoeightnine.root.xvii.utils

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.RelativeLayout
import android.widget.TextView

class BottomSheetHelper(private var bottomSheet: RelativeLayout,
                        thumb: RelativeLayout,
                        private var title: TextView,
                        private var container: Int,
                        private var fragmentManager: androidx.fragment.app.FragmentManager,
                        private var bottomSheetHeightPx: Int) {

    companion object {
        const val DURATION = 300L
    }

    private var x: Float = 0f
    private var y: Float = 0f

    init {
        bottomSheet.visibility = View.GONE
        thumb.setOnClickListener { closeBottomSheet() }
        thumb.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.x
                    y = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    closeBottomSheet()
                    true
                }
                else -> false
            }
        }
    }

    fun openBottomSheet(frag: androidx.fragment.app.Fragment, title: String = "") {
        if (!isBottomOpen()) {
            val bottomParams = bottomSheet.layoutParams as ViewGroup.MarginLayoutParams
            bottomParams.bottomMargin = -(bottomSheetHeightPx)
            bottomSheet.show()
            val animator = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    super.applyTransformation(interpolatedTime, t)
                    bottomParams.bottomMargin =
                            -((bottomSheetHeightPx) * (1 - interpolatedTime)).toInt()
                    bottomSheet.layoutParams = bottomParams
                }
            }
            animator.duration = DURATION
            animator.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    loadFragment(frag, title)
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
            bottomSheet.startAnimation(animator)
        } else {
            loadFragment(frag, title)
        }
    }

    private fun loadFragment(frag: androidx.fragment.app.Fragment, title: String = "") {
        fragmentManager
                .beginTransaction()
                .replace(container, frag)
                .commit()
        this.title.text = title
    }

    private fun removeFragment() {
        try {
            fragmentManager
                    .beginTransaction()
                    .remove(fragmentManager.findFragmentById(container)!!)
                    .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeBottomSheet() {
        if (isBottomOpen()) {
            val bottomParams = bottomSheet.layoutParams as ViewGroup.MarginLayoutParams
            val actualHeight = bottomSheetHeightPx + bottomParams.bottomMargin
            val animator = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    super.applyTransformation(interpolatedTime, t)
                    bottomParams.bottomMargin =
                            -((actualHeight) * interpolatedTime).toInt()
                    bottomSheet.layoutParams = bottomParams
                }
            }
            animator.duration = DURATION
            animator.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    bottomSheet.hide()
                    removeFragment()
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
            bottomSheet.startAnimation(animator)
        } else {
            removeFragment()
        }
    }

    fun isBottomOpen() = bottomSheet.visibility == View.VISIBLE
}