package com.twoeightnine.root.xvii.views

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.StatTool
import com.twoeightnine.root.xvii.utils.rate
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.dialog_rate.view.*

class RateAlertDialog(context: Context) : AlertDialog(context) {

    init {
        val view = View.inflate(context, R.layout.dialog_rate, null)
        with(view) {
            tvRate.setOnClickListener {
                rate(context)
                Prefs.showRate = false
                dismiss()
            }
            tvNotNow.setOnClickListener {
                dismiss()
            }
            tvNever.setOnClickListener {
                Prefs.showRate = false
                dismiss()
            }
        }
        setView(view)
    }

    override fun show() {
        val launches = StatTool.get()?.launches ?: 0
        if (Prefs.showRate && launches > 2) {
            super.show()
            stylize()
        }
    }
}