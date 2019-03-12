package com.twoeightnine.root.xvii.views

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.rate
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

}