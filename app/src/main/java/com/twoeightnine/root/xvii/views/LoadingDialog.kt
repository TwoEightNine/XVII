package com.twoeightnine.root.xvii.views

import android.app.AlertDialog
import android.content.Context
import android.view.View
import com.twoeightnine.root.xvii.R
import global.msnthrp.xvii.uikit.extensions.hide
import kotlinx.android.synthetic.main.dialog_loading.view.*

class LoadingDialog(context: Context,
                    text: String = "",
                    cancelable: Boolean = false) : AlertDialog(context) {

    init {
        val view = View.inflate(context, R.layout.dialog_loading, null)
        with(view) {
            if (text.isNotEmpty()) {
                tvTitle.text = text
            } else {
                tvTitle.hide()
            }
        }
        setCancelable(cancelable)
        setView(view)
    }

}