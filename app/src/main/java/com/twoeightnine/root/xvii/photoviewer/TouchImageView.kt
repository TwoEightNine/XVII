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

package com.twoeightnine.root.xvii.photoviewer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.min

class TouchImageView : AppCompatImageView {

    private lateinit var mMatrix: Matrix
    private var mode = NONE

    private lateinit var m: FloatArray

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var saveScale = 1f
    private var origWidth = 0f
    private var origHeight = 0f
    private var oldMeasuredWidth: Int = 0
    private var oldMeasuredHeight: Int = 0

    private lateinit var mScaleDetector: ScaleGestureDetector

    var callback: InteractionCallback? = null

    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        sharedConstructing(context)
    }

    private fun sharedConstructing(context: Context) {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mMatrix = Matrix()
        m = FloatArray(9)
        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX
        setOnTouchListener(TouchListener())
    }

    private fun fixTrans() {
        mMatrix.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), origWidth * saveScale)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), origHeight * saveScale)

        if (fixTransX != 0f || fixTransY != 0f)
            mMatrix.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans)
            return -trans + minTrans
        if (trans > maxTrans)
            return -trans + maxTrans
        return 0f
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        if (contentSize <= viewSize) {
            return 0f
        }
        return delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return
        oldMeasuredHeight = viewHeight
        oldMeasuredWidth = viewWidth

        if (saveScale == 1f) {
            // Fit to screen.
            val scale: Float

            val drawable = drawable
            if (drawable == null || drawable.intrinsicWidth == 0
                    || drawable.intrinsicHeight == 0)
                return
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight

            Log.d("bmSize", "bmWidth: $bmWidth bmHeight : $bmHeight")

            val scaleX = viewWidth.toFloat() / bmWidth.toFloat()
            val scaleY = viewHeight.toFloat() / bmHeight.toFloat()
            scale = min(scaleX, scaleY)
            mMatrix.setScale(scale, scale)

            // Center the image
            var redundantYSpace = viewHeight.toFloat() - scale * bmHeight.toFloat()
            var redundantXSpace = viewWidth.toFloat() - scale * bmWidth.toFloat()
            redundantYSpace /= 2.toFloat()
            redundantXSpace /= 2.toFloat()

            mMatrix.postTranslate(redundantXSpace, redundantYSpace)

            origWidth = viewWidth - 2 * redundantXSpace
            origHeight = viewHeight - 2 * redundantYSpace
            imageMatrix = mMatrix
        }
        fixTrans()
    }

    fun canSlide() = saveScale > 1.1f

    fun toggleScale(focus: PointF) {
        if (saveScale >= (MAX_SCALE + MIN_SCALE) / 2) {
            ValueAnimator.ofFloat(saveScale, MIN_SCALE)
        } else {
            ValueAnimator.ofFloat(saveScale, MAX_SCALE)
        }.apply {
            duration = 200L
            addUpdateListener { animator ->
                val scale = animator.animatedValue as Float
                val scaleFactor = scale / saveScale
                scaleImage(scaleFactor, focus)
                imageMatrix = mMatrix
                invalidate()
            }
            start()
        }
    }

    private fun scaleImage(scaleFactor: Float, focus: PointF) {
        var usableScaleFactor = scaleFactor
        val origScale = saveScale
        saveScale *= usableScaleFactor
        if (saveScale > MAX_SCALE) {
            saveScale = MAX_SCALE
            usableScaleFactor = MAX_SCALE / origScale
        } else if (saveScale < MIN_SCALE) {
            saveScale = MIN_SCALE
            usableScaleFactor = MIN_SCALE / origScale
        }

        if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
            mMatrix.postScale(
                    usableScaleFactor,
                    usableScaleFactor,
                    (viewWidth / 2).toFloat(),
                    (viewHeight / 2).toFloat()
            )
        } else {
            mMatrix.postScale(
                    usableScaleFactor,
                    usableScaleFactor,
                    focus.x,
                    focus.y
            )
        }
        fixTrans()
    }

    interface InteractionCallback {
        fun onTap()
        fun onDoubleTap()
        fun onDismiss()
    }

    companion object {

        const val MIN_SCALE = 1f
        const val MAX_SCALE = 3f

        // We can be in one of these 3 states
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
        private const val CLICK = 3

        private const val DISMISS_MIN = 200
        private const val DOUBLE_TAP_DELAY = 200L
        private const val POSTPONED_TAP_DELAY = 250L
    }

    private inner class TouchListener : OnTouchListener {

        private var last = PointF()
        private var start = PointF()

        private var lastTap = 0L

        private val mainHandler = Handler(Looper.getMainLooper())
        private val tapRunnable = Runnable {
            performClick()
            callback?.onTap()
        }

        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            mScaleDetector.onTouchEvent(event)
            val curr = PointF(event.x, event.y)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }

                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    val deltaX = curr.x - last.x
                    val deltaY = curr.y - last.y
                    val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(),
                            origWidth * saveScale)
                    val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(),
                            origHeight * saveScale)
                    mMatrix.postTranslate(fixTransX, fixTransY)
                    fixTrans()
                    last.set(curr.x, curr.y)
                }

                MotionEvent.ACTION_UP -> {
                    val xDiff = abs(curr.x - start.x).toInt()
                    val yDiff = abs(curr.y - start.y).toInt()
                    if (xDiff * 2 < yDiff && yDiff > DISMISS_MIN && mode == DRAG && !canSlide()) {
                        callback?.onDismiss()
                    }
                    mode = NONE
                    if (xDiff < CLICK && yDiff < CLICK) {
                        val isDoubleClick = System.currentTimeMillis() - lastTap < DOUBLE_TAP_DELAY
                        if (isDoubleClick) {
                            callback?.onDoubleTap()
                            mainHandler.removeCallbacks(tapRunnable)
                            toggleScale(last)
                        } else {
                            lastTap = System.currentTimeMillis()
                            mainHandler.postDelayed(tapRunnable, POSTPONED_TAP_DELAY)
                        }
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }

            imageMatrix = mMatrix
            invalidate()
            return true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleImage(
                    detector.scaleFactor,
                    PointF(detector.focusX, detector.focusY)
            )
            return true
        }
    }
}
