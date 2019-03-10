package com.twoeightnine.root.xvii.views

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.crypto.CryptoUtil
import com.twoeightnine.root.xvii.utils.getUiFriendlyHash
import com.twoeightnine.root.xvii.utils.loadUrl

/**
 * Created by fuck that shit boy on 07.12.2017.
 */
class FingerPrintAlertDialog(context: Context,
                             fingerprint: String,
                             keyType: CryptoUtil.KeyType) : AlertDialog(context) {

    @BindView(R.id.ivGravatar)
    lateinit var ivGravatar: ImageView
    @BindView(R.id.tvPrint)
    lateinit var tvPrint: TextView
    @BindView(R.id.tvKeyType)
    lateinit var tvKeyType: TextView

    init {
        val view = View.inflate(context, R.layout.dialog_fingerprint, null)
        ButterKnife.bind(this, view)
        setView(view)
        tvPrint.text = getUiFriendlyHash(fingerprint)
        ivGravatar.loadUrl("https://www.gravatar.com/avatar/$fingerprint?s=256&d=identicon&r=PG")
        tvKeyType.text = context.getString(R.string.key_type, context.getString(keyType.stringRes).toUpperCase())
        if (keyType == CryptoUtil.KeyType.DEFAULT) {
            tvKeyType.setTextColor(ContextCompat.getColor(context, R.color.error))
        }
        view.setOnClickListener { dismiss() }
    }

}