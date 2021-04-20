package com.twoeightnine.root.xvii.views

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.StatTool
import com.twoeightnine.root.xvii.utils.rate
import com.twoeightnine.root.xvii.utils.stylize

class RateAlertDialog(context: Context) : AlertDialog(context) {

    init {
        setMessage(context.getString(R.string.wanna_rate))
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.rate)) { _, _ ->
            rate(context)
            Prefs.showRate = false
        }
        setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.not_now)) { _, _ -> }
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.never)) { _, _ ->
            Prefs.showRate = false
        }
    }

    override fun show() {
        val launches = StatTool.get()?.launches ?: 0
        if (Prefs.showRate && launches > 2) {
            super.show()
            stylize()
        }
    }
}