package com.twoeightnine.root.xvii.views

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.crypto.getUiFriendlyHash
import com.twoeightnine.root.xvii.utils.loadRounded
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.dialog_fingerprint.view.*

/**
 * Created by fuck that shit boy on 07.12.2017.
 */
class FingerPrintAlertDialog(context: Context,
                             fingerprint: String,
                             keyType: CryptoEngine.KeyType) : AlertDialog(context) {

    init {
        val view = View.inflate(context, R.layout.dialog_fingerprint, null)
        setView(view)
        with(view) {
            tvPrint.text = getUiFriendlyHash(fingerprint)
            ivGravatar.loadRounded("https://www.gravatar.com/avatar/$fingerprint?s=256&d=identicon&r=PG")
            tvKeyType.text = context.getString(R.string.key_type, context.getString(keyType.stringRes))
            if (keyType == CryptoEngine.KeyType.DEFAULT) {
                tvKeyType.setTextColor(ContextCompat.getColor(context, R.color.error))
            }
            setOnClickListener { dismiss() }
        }
    }

    override fun show() {
        super.show()
        stylize()
    }
}