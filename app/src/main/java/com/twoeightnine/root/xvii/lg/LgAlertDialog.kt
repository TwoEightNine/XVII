package com.twoeightnine.root.xvii.lg

import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.copyToClip

class LgAlertDialog(context: Context) : AlertDialog(context) {

    init {
        setMessage(Lg.getEvents())
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            dismiss()
        }
        setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.copy)) { _, _ ->
            copyToClip(Lg.getEvents())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<TextView>(android.R.id.message)?.apply {
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE)
        }
    }

    companion object {
        const val FONT_SIZE = 12f
    }

}