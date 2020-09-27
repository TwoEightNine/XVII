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

    fun hideOk() {
        getChildAt(childCount - 1).visibility = View.GONE
    }

    private fun init() {
        columnCount = 3
        orientation = HORIZONTAL
        rowCount = 4

        for (i in 1..12) {
            val view = View.inflate(context, R.layout.item_pinpad, null)
            val tv = view.findViewById<TextView>(R.id.tvNum)
            if (i <= 10) {
                tv.text = "${i % 10}"
            } else {
                tv.setText(if (i == 11) R.string.clear else R.string.ok)
            }
            view.setOnClickListener {
                if (listener != null) {
                    if (i <= 10) {
                        listener?.invoke(i % 10)
                    } else {
                        listener?.invoke(if (i == 11) DELETE else OK)
                    }
                }
            }
            addView(view)
        }

    }

    companion object {

        const val OK = -1
        const val DELETE = -2
    }

}
