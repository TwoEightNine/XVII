package com.twoeightnine.root.xvii.views

import android.app.AlertDialog
import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.dialog_loading.*

class LoadingDialog(context: Context,
                    text: String = "",
                    cancelable: Boolean = false): AlertDialog(context) {

        init {
            val view = View.inflate(context, R.layout.dialog_loading, null)
            if (text.isNotEmpty()) {
                tvTitle.text = text
            } else {
                tvTitle.visibility = View.GONE
            }
            setCancelable(cancelable)
            setView(view)
        }

}