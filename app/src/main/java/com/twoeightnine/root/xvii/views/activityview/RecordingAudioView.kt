/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.views.activityview

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint


class RecordingAudioView : LinearLayout {

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
            dot.setImageDrawable(createDrawable())
            dot.layoutParams = LayoutParams(dotSize, dotSize).apply {
                setMargins(DOTS_MARGIN, DOTS_MARGIN, DOTS_MARGIN, DOTS_MARGIN)
                gravity = Gravity.CENTER
            }
            addView(dot)
            ObjectAnimator.ofPropertyValuesHolder(dot,
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, SCALE)
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
                view.paint(Munch.color.color)
            }
        }
    }

    private fun createDrawable() = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 2f
        setColor(Color.WHITE)
    }

    companion object {

        private const val DURATION = 200L
        private const val SCALE = 1.6f

        private const val DOTS_COUNT = 4
        private const val DOTS_MARGIN = 3
    }
}