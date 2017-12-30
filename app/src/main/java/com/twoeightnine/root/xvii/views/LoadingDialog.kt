package com.twoeightnine.root.xvii.views

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R

class LoadingDialog(context: Context,
                    text: String = "",
                    cancelable: Boolean = false): AlertDialog(context) {

    @BindView(R.id.tvTitle)
    lateinit var tvTitle: TextView

        init {
            val view = View.inflate(context, R.layout.dialog_loading, null)
            ButterKnife.bind(this, view)
            if (text.isNotEmpty()) {
                tvTitle.text = text
            } else {
                tvTitle.visibility = View.GONE
            }
            setCancelable(cancelable)
            setView(view)
        }

}