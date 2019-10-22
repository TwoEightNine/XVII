package com.twoeightnine.root.xvii.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.CompoundButton
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.view_switch.view.*


class XviiSwitch(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    var onCheckedListener: CompoundButton.OnCheckedChangeListener? = null
        set(value) {
            field = value
            switchCompat.setOnCheckedChangeListener(value)
        }

    var isChecked: Boolean
        get() = switchCompat.isChecked
        set(value) {
            switchCompat.isChecked = value
        }

    init {
        View.inflate(context, R.layout.view_switch, this)
        setOnClickListener { switchCompat.toggle() }

        val attrsArray = intArrayOf(
                android.R.attr.text, // 0
                android.R.attr.hint // 1
        )
        val ta = context.obtainStyledAttributes(attributeSet, attrsArray)
        val text = ta.getText(0)
        val hint = ta.getText(1)
        ta.recycle()

        tvTitle.text = text
        tvHint.text = hint
        if (hint.isNullOrEmpty()) {
            tvHint.hide()
        }

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)

        switchCompat.stylize()
    }

    override fun isEnabled() = switchCompat.isEnabled

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        switchCompat.isEnabled = enabled
    }
}