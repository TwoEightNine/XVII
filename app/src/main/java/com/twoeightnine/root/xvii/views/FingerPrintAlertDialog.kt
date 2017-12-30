package com.twoeightnine.root.xvii.views

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.getUiFriendlyHash
import com.twoeightnine.root.xvii.utils.loadUrl

/**
 * Created by fuck that shit boy on 07.12.2017.
 */
class FingerPrintAlertDialog(context: Context,
                             fingerprint: String) : AlertDialog(context) {

    @BindView(R.id.ivGravatar)
    lateinit var ivGravatar: ImageView
    @BindView(R.id.tvPrint)
    lateinit var tvPrint: TextView

    init {
        val view = View.inflate(context, R.layout.dialog_fingerprint, null)
        ButterKnife.bind(this, view)
        setView(view)
        tvPrint.text = getUiFriendlyHash(fingerprint)
        ivGravatar.loadUrl("https://www.gravatar.com/avatar/$fingerprint?s=256&d=identicon&r=PG")
        view.setOnClickListener { dismiss() }
    }

}