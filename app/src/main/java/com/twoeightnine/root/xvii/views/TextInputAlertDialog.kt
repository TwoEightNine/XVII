package com.twoeightnine.root.xvii.views

import android.app.AlertDialog
import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Style

class TextInputAlertDialog(context: Context,
                           var title: String,
                           hint: String,
                           var listener: ((String) -> Unit)?,
                           useFilter: Boolean = true): AlertDialog(context) {

    @BindView(R.id.tvTitle)
    lateinit var tvTitle: TextView
    @BindView(R.id.etInput)
    lateinit var etInput: EditText
    @BindView(R.id.ivDone)
    lateinit var ivDone: ImageView

    init {
        val view = View.inflate(context, R.layout.dialog_enter_text, null)
        ButterKnife.bind(this, view)
        ivDone.setOnClickListener {
            listener?.invoke(etInput.text.toString())
            dismiss()
        }
        tvTitle.text = title
        etInput.hint = hint
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
        setView(view)
        if (useFilter) { //kostylyok
            Style.forImageView(ivDone, Style.DARK_TAG)
        }
    }

    private fun isPrintable(c: Char)
            = c.toByte() in 48..57 ||
            c.toByte() in 65..90 ||
            c.toByte() in 97..122

}