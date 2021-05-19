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

package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.text.Layout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.twoeightnine.root.xvii.lg.L
import kotlin.math.roundToInt

class AccurateTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {


    private var maxLineWidth: Int? = null
    private var lastLineWidth: Int? = null
    private var linesCount: Int? = null
    private var calculated = false

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val maxLineWidth = maxLineWidth
//        if (maxLineWidth != null) {
//            L.def().log("setMeasured $maxLineWidth, '$text'")
//            setMeasuredDimension(maxLineWidth, measuredHeight)
//        } else {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        }
//    }

//    override fun setText(text: CharSequence?, type: BufferType?) {
//        if (text != this.text) {
//            calculated = false
//            L.def().log("non equal '$text', '${this.text}'")
//        } else {
//            L.def().log("equal '$text', '${this.text}'")
//        }
//        super.setText(text, type)
//        if (!calculated) {
//            calculated = true
//            if (layout != null) {
//                L.def().log("just non null, '$text'")
//                recalculate()
//            } else {
//                onPreDraw {
//                    if (layout != null) {
//                        L.def().log("predraw non null, '$text'")
//                        recalculate()
//                    }
//                }
//            }
//        }
//    }

    private fun recalculate() {
        maxLineWidth = getMaxLineWidth(layout)
        lastLineWidth = getLastLineWidth(layout)
        linesCount = layout.lineCount

        L.def().log("$maxLineWidth, $lastLineWidth, '$text'")
        requestLayout()
        invalidate()
    }

    //    override fun onMeasure(
//            widthMeasureSpec: Int,
//            heightMeasureSpec: Int
//    ) {
//        if (layout == null || layout.lineCount < 2) {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//            return
//        }
//
//        val maxLineWidth = ceil(getMaxLineWidth(layout)).toInt()
//        val uselessPaddingWidth = layout.width - maxLineWidth
//        val extraDeltaToCarryLine = when {
//            isLastLineAlmostFullWidth(layout) -> CARRY_LINE_DELTA
//            else -> 0
//        }
//
//        val width = measuredWidth - uselessPaddingWidth - extraDeltaToCarryLine
//
//        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
//        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
//        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
//    }

    private fun getExtraSpaceOnLastLine(layout: Layout): Int {
        return getMaxLineWidth(layout) - getLastLineWidth(layout)
    }

    private fun getLastLineWidth(layout: Layout): Int {
        return layout.getLineWidth(layout.lineCount.dec()).roundToInt()
    }

    private fun getMaxLineWidth(layout: Layout): Int {
        return (0 until layout.lineCount)
                .map(layout::getLineWidth)
                .maxOrNull()
                ?.roundToInt()
                ?: 0
    }
}