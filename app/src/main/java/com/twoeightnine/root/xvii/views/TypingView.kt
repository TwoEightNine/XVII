package com.twoeightnine.root.xvii.views

import android.animation.*
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.ColorManager
import com.twoeightnine.root.xvii.utils.stylize

class TypingView : LinearLayout {

    private var cancelled = false
    private var animatorSet: AnimatorSet? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        orientation = HORIZONTAL

        val dotSize = context.resources.getDimensionPixelSize(R.dimen.typing_dot_size)
        val animators = arrayListOf<Animator>()
        val animatorSet = AnimatorSet()
        for (i in 0 until DOTS_COUNT) {
            val dot = ImageView(context)
            dot.setImageResource(R.drawable.shape_typing_dot)
            val lp = LayoutParams(dotSize, dotSize)
            lp.setMargins(DOTS_MARGIN, DOTS_MARGIN, DOTS_MARGIN, DOTS_MARGIN)
            lp.gravity = Gravity.CENTER
            dot.layoutParams = lp
            addView(dot)
            ObjectAnimator.ofPropertyValuesHolder(dot,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, SCALE),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, SCALE)
            ).apply {
                repeatMode = ValueAnimator.REVERSE
                repeatCount = 1
                animators.add(this)
            }
        }
        post {
            animatorSet.playSequentially(animators)
            animatorSet.duration = DURATION
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!cancelled) {
                        animatorSet.start()
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }
            })
            animatorSet.start()
            this.animatorSet = animatorSet
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        cancelled = false
        animatorSet?.start()
    }

    override fun onDetachedFromWindow() {
        cancelled = true
        animatorSet?.cancel()
        super.onDetachedFromWindow()
    }

    fun stylize() {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view is ImageView) {
                view.stylize(tag = ColorManager.MAIN_TAG, changeStroke = false)
            }
        }
    }

    companion object {

        private const val DURATION = 200L
        private const val SCALE = 1.3f

        private const val DOTS_COUNT = 3
        private const val DOTS_MARGIN = 4
    }
}