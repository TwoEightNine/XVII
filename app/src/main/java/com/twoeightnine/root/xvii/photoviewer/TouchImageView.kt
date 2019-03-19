package com.twoeightnine.root.xvii.photoviewer

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

class TouchImageView : AppCompatImageView {

    lateinit var mMatrix: Matrix
    internal var mode = NONE

    // Remember some things for zooming
    internal var last = PointF()
    internal var start = PointF()
    internal var minScale = 1f
    internal var maxScale = 3f
    lateinit var m: FloatArray

    internal var viewWidth: Int = 0
    internal var viewHeight: Int = 0
    internal var saveScale = 1f
    protected var origWidth: Float = 0.toFloat()
    protected var origHeight: Float = 0.toFloat()
    internal var oldMeasuredWidth: Int = 0
    internal var oldMeasuredHeight: Int = 0

    lateinit var mScaleDetector: ScaleGestureDetector

    lateinit var mContext: Context

    var dismissListener: (() -> Unit)? = null
    var tapListener: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        sharedConstructing(context)
    }

    private fun sharedConstructing(context: Context) {
        super.setClickable(true)
        this.mContext = context
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mMatrix = Matrix()
        m = FloatArray(9)
        imageMatrix = mMatrix
        scaleType = ImageView.ScaleType.MATRIX

        setOnTouchListener { _, event ->
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
                    val xDiff = Math.abs(curr.x - start.x).toInt()
                    val yDiff = Math.abs(curr.y - start.y).toInt()
                    if (xDiff * 2 < yDiff && yDiff > DISMISS_MIN && mode == DRAG && !canSlide()) {
                        dismissListener?.invoke()

                    }
                    mode = NONE
                    if (xDiff < CLICK && yDiff < CLICK) {
                        performClick()
                        tapListener?.invoke()
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }

            imageMatrix = mMatrix
            invalidate()
            true // indicate event was handled
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }

            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)
                mMatrix.postScale(mScaleFactor, mScaleFactor, (viewWidth / 2).toFloat(),
                        (viewHeight / 2).toFloat())
            else
                mMatrix.postScale(mScaleFactor, mScaleFactor,
                        detector.focusX, detector.focusY)

            fixTrans()
            return true
        }
    }

    internal fun fixTrans() {
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
        viewWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = View.MeasureSpec.getSize(heightMeasureSpec)

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
            scale = Math.min(scaleX, scaleY)
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

    companion object {

        // We can be in one of these 3 states
        internal val NONE = 0
        internal val DRAG = 1
        internal val ZOOM = 2
        internal val CLICK = 3
        internal val DISMISS_MIN = 200
    }
}
