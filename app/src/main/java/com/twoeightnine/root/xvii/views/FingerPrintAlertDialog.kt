package com.twoeightnine.root.xvii.views

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.dialog_fingerprint.view.*

class FingerPrintAlertDialog(context: Context,
                             fingerprint: String) : AlertDialog(context) {

    init {
        View.inflate(context, R.layout.dialog_fingerprint, null).apply {
            setView(this)
            tvPrint.text = getUiFriendlyHash(fingerprint)
            ivGravatar.load("https://www.gravatar.com/avatar/$fingerprint?s=256&d=identicon&r=PG")
            setOnClickListener { dismiss() }
        }
    }

    override fun show() {
        super.show()
        stylize()
    }

    private fun getUiFriendlyHash(hash: String) = hash
            .mapIndexed { index, c -> if (index % 2 == 0) c.toString() else "$c " } // spaces
            .mapIndexed { index, s -> if (index % 16 == 15) "$s\n" else s } // new lines
            .joinToString(separator = "")
}