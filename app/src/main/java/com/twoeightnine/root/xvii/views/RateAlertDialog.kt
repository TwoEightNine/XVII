package com.twoeightnine.root.xvii.views

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.rate

class RateAlertDialog(context: Context): AlertDialog(context) {

    @BindView(R.id.tvRate)
    lateinit var tvRate: TextView
    @BindView(R.id.tvNotNow)
    lateinit var tvNotNow: TextView
    @BindView(R.id.tvNever)
    lateinit var tvNever: TextView

    init {
        val view = View.inflate(context, R.layout.dialog_rate, null)
        ButterKnife.bind(this, view)
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

        setView(view)
    }

}