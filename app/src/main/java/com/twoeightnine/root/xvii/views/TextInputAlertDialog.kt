package com.twoeightnine.root.xvii.views

import android.app.AlertDialog
import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Style
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.dialog_enter_text.view.*

class TextInputAlertDialog(context: Context,
                           var title: String,
                           hint: String,
                           presetText: String = "",
                           var listener: ((String) -> Unit)? = null,
                           stylize: Boolean = true,
                           useFilter: Boolean = false) : AlertDialog(context) {

    init {
        val view = View.inflate(context, R.layout.dialog_enter_text, null)
        with(view) {
            ivDone.setOnClickListener {
                listener?.invoke(etInput.text.toString())
                dismiss()
            }
            tvTitle.text = title
            etInput.hint = hint
            etInput.setText(presetText)
            if (useFilter) {
                val filter = object : InputFilter {
                    override fun filter(source: CharSequence?, start: Int, end: Int, p3: Spanned?, p4: Int, p5: Int): CharSequence? {
                        if (source == null) return null
                        for (i in start until end) {
                            if (!isPrintable(source[i])) {
                                return ""
                            }
                        }
                        return null
                    }
                }
                etInput.filters = arrayOf(filter)
            }
            if (stylize) {
                Style.forImageView(ivDone, Style.DARK_TAG)
                val d2 = rlInputContainer.background
                Style.forFrame(d2)
                rlInputContainer.background = d2
            }
        }
        setView(view)
    }

    private fun isPrintable(c: Char) = c.toByte() in 48..57 ||
            c.toByte() in 65..90 ||
            c.toByte() in 97..122

}