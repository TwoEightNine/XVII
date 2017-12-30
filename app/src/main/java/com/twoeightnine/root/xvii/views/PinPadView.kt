package com.twoeightnine.root.xvii.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout
import android.widget.TextView

import com.twoeightnine.root.xvii.R

/**
 * Created by root on 3/17/17.
 */

class PinPadView : GridLayout {

    var listener: ((Int) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        columnCount = 3
        orientation = GridLayout.HORIZONTAL
        rowCount = 4

        for (i in 1..12) {
            val view = View.inflate(context, R.layout.item_pinpad, null)
            val tv = view.findViewById<TextView>(R.id.tvNum)
            if (i <= 10) {
                tv.text = "${i % 10}"
            } else {
                tv.setText(if (i == 11) R.string.clear else android.R.string.ok)
            }
            val pos = i
            view.setOnClickListener { _ ->
                if (listener != null) {
                    if (pos <= 10) {
                        listener?.invoke(pos % 10)
                    } else {
                        listener?.invoke(if (pos == 11) DELETE else OK)
                    }
                }
            }
            addView(view)
        }

    }

    companion object {

        val OK = -1
        val DELETE = -2
    }

}
