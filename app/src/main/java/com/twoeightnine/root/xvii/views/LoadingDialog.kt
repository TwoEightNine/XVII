package com.twoeightnine.root.xvii.views

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize
import global.msnthrp.xvii.uikit.extensions.hide
import kotlinx.android.synthetic.main.dialog_loading.view.*

class LoadingDialog(context: Context,
                    text: String = "",
                    cancelable: Boolean = false) : AlertDialog(context) {

    init {
        View.inflate(context, R.layout.dialog_loading, null).apply {
            if (text.isNotEmpty()) {
                tvTitle.text = text
            } else {
                tvTitle.hide()
            }
            setCancelable(cancelable)
            setView(this)
        }
    }

    override fun show() {
        super.show()
        stylize()
    }
}