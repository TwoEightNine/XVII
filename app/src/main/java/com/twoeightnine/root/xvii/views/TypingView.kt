package com.twoeightnine.root.xvii.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style

class TypingView : LinearLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        init()
    }

    private fun init() {
        orientation = VERTICAL

        val view = View.inflate(context, R.layout.view_typing, this)
        val iv1 = view.findViewById<ImageView>(R.id.ivDot1)
        val iv2 = view.findViewById<ImageView>(R.id.ivDot2)
        val iv3 = view.findViewById<ImageView>(R.id.ivDot3)
        Style.forViewGroupColor(view.findViewById<RelativeLayout>(R.id.rlBack))

        ValueAnimator.ofFloat(0f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            duration = DURATION
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                iv1.setColor(getColor(value < EDGE_1))
            }
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                iv2.setColor(getColor(value >= EDGE_1 && value < EDGE_2))
            }
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                iv3.setColor(getColor(value >= EDGE_2))
            }
        }.start()
    }

    private fun getColor(isWhite: Boolean) = when {
        isWhite -> Color.WHITE
        Prefs.isLightTheme -> Color.LTGRAY
        else -> Color.GRAY
    }

    private fun ImageView.setColor(color: Int) {
        (drawable as? GradientDrawable)?.setColor(color)
    }

    companion object {
        const val DURATION = 2000L
        const val EDGE_1 = 0.27f
        const val EDGE_2 = 0.73f
    }
}