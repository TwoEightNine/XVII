package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.text.Layout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.ceil

class AccurateTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {


    override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int
    ) {
        if (layout == null || layout.lineCount < 2) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val maxLineWidth = ceil(getMaxLineWidth(layout)).toInt()
        val uselessPaddingWidth = layout.width - maxLineWidth
        val extraDeltaToCarryLine = when {
            isLastLineAlmostFullWidth(layout) -> CARRY_LINE_DELTA
            else -> 0
        }

        val width = measuredWidth - uselessPaddingWidth - extraDeltaToCarryLine

        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }

    private fun getMaxLineWidth(layout: Layout): Float {
        return (0 until layout.lineCount)
                .map { layout.getLineWidth(it) }
                .maxOrNull() ?: 0.0f
    }

    private fun isLastLineAlmostFullWidth(layout: Layout): Boolean {
        return layout.width - layout.getLineWidth(layout.lineCount - 1) < LAST_LINE_THRESHOLD
    }

    companion object {
        private const val LAST_LINE_THRESHOLD = 200
        private const val CARRY_LINE_DELTA = 20
    }
}