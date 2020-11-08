package com.twoeightnine.root.xvii.utils

import android.animation.ValueAnimator
import android.app.Activity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import global.msnthrp.xvii.uikit.extensions.EndAnimatorListener

/**
 * allows to move view according to user's finger motions
 * used to provide swipe-to-back behavior
 */
class DragTouchListener(
        private val activity: Activity,
        private val movableView: View,
        private val backgroundShadow: View
) : View.OnTouchListener {

    private var screenWidthPx = activity.resources.displayMetrics.widthPixels

    private var isKeyboardHidden = false
    private val tracker = MotionTracker()

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event ?: return false

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                tracker.reset()
                tracker.update(event.x)
                isKeyboardHidden = false
                true
            }
            MotionEvent.ACTION_MOVE -> {
                tracker.update(event.x)
                applyValue(tracker.x / screenWidthPx)
                if (!isKeyboardHidden) {
                    hideKeyboard(activity)
                    isKeyboardHidden = true
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                val currentValue = tracker.x / screenWidthPx
                if (tracker.canReachX(screenWidthPx / 2f)) {
                    animate(currentValue, 1f, withFinish = true)
                } else {
                    animate(currentValue, 0f)
                }
                true
            }
            else -> false
        }
    }

    /**
     * adjusts shadow and translation
     * @param value part of screen passed by finger in pixels
     */
    private fun applyValue(value: Float) {
        movableView.translationX = value * screenWidthPx
        backgroundShadow.alpha = 1 - value
    }

    /**
     * animates collapsing view
     *
     * @param from current value of finger
     * @param to 0f or 1f according to direction (0f -- return to left, 1f -- close view to right)
     * @param withFinish if [activity] should be finished
     */
    private fun animate(from: Float, to: Float, withFinish: Boolean = false) {
        ValueAnimator.ofFloat(from, to).apply {
            duration = DURATION_COLLAPSE
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                applyValue(animatedValue as Float)
            }
            if (withFinish) {
                addListener(EndAnimatorListener {
                    activity.finish()
                })
            }
            start()
        }
    }

    /**
     * tracks position, calculates velocity
     *
     * @param d2x acceleration of movable view (for gesture dynamics)
     */
    private class MotionTracker(private val d2x: Float = -12f) {

        var x = 0f
            private set

        var dx = 0f
            private set

        /**
         * update position [x] and velocity [dx]
         */
        fun update(x: Float) {
            this.dx = x - this.x
            this.x = x
        }

        /**
         * reset position [x] and velocity [dx]
         * used with [MotionEvent.ACTION_DOWN]
         */
        fun reset() {
            this.x = 0f
            this.dx = 0f
        }

        /**
         * calculates if pointer (in theory) can reach [xToReach] with given
         * position [x], velocity [dx] and acceleration [d2x]
         *
         * this inequation represents condition of non-negative discriminant of
         * quadratic equation of movement
         */
        fun canReachX(xToReach: Float): Boolean {
            Log.i("qwer", "x = $x; dx = $dx; dx**2 = ${dx * dx}; x - x = ${xToReach - this.x}")
            return dx > 0 && dx * dx + 2 * d2x * (xToReach - this.x) >= 0
        }

    }

    companion object {

        const val DURATION_COLLAPSE = 150L
    }
}