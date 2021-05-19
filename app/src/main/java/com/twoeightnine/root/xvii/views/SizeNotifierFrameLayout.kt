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

package com.twoeightnine.root.xvii.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * layout that will not shrink background image when keyboard is open
 */
class SizeNotifierFrameLayout : FrameLayout {

    var backgroundImage: Drawable? = null
        set(bitmap) {
            field = bitmap
            invalidate()
        }
    private var keyboardHeight: Int = 0
    private var isKbOpen = false

    private var actualHeight = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    fun init() {
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (actualHeight == 0) {
            actualHeight = height
            return
        }
        //kb detected
        if (actualHeight - height > 100 && keyboardHeight == 0) {
            keyboardHeight = actualHeight - height
            isKbOpen = true
        }
        if (actualHeight - height < 50 && keyboardHeight != 0) {
            isKbOpen = false
        }
        if (height != actualHeight) {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        backgroundImage ?: super.onDraw(canvas)

        backgroundImage?.let { backgroundImage ->
            if (backgroundImage is ColorDrawable) {
                backgroundImage.setBounds(0, 0, measuredWidth, measuredHeight)
                backgroundImage.draw(canvas)
            } else if (backgroundImage is BitmapDrawable) {
                val scale = measuredWidth.toFloat() / backgroundImage.intrinsicWidth.toFloat()
                val width = Math.ceil((backgroundImage.intrinsicWidth * scale).toDouble()).toInt()
                val height = Math.ceil((backgroundImage.intrinsicHeight * scale).toDouble()).toInt()
                backgroundImage.setBounds(0, 0, width, height)
                backgroundImage.draw(canvas)
            }
        }
    }
}