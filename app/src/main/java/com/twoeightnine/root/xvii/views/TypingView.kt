package com.twoeightnine.root.xvii.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Style

class TypingView : LinearLayout {

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attributes: AttributeSet): super(context, attributes) {
        init()
    }


    private fun init() {
        orientation = VERTICAL

        val view = View.inflate(context, R.layout.view_typing, this)
        val iv1 = view.findViewById<ImageView>(R.id.ivDot1)
        val iv2 = view.findViewById<ImageView>(R.id.ivDot2)
        val iv3 = view.findViewById<ImageView>(R.id.ivDot3)

        Style.forViewGroupColor(view.findViewById<RelativeLayout>(R.id.rlBack))

        val scale = AnimationUtils.loadAnimation(context, R.anim.scale_loader)
        val scale50 = AnimationUtils.loadAnimation(context, R.anim.scale_loader_50)

        iv1.startAnimation(scale)
        iv2.startAnimation(scale50)
        iv3.startAnimation(scale)
    }

}