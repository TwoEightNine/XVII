package com.twoeightnine.root.xvii.egg.scream

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize

class ScreamAlertDialog(
        context: Context,
        private val state: Int = STATE_ASK,
        private val onScreamEnabled: () -> Unit
) : AlertDialog(context) {

    init {
        setCancelable(false)
        setMessage(context.getString(getMessage()))
        if (shouldAddNo()) {
            setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.no)) { _, _ -> }
        }
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(getOkText())) { _, _ ->
            onOkClicked()
        }
    }

    override fun show() {
        super.show()
        stylize()
    }

    private fun shouldAddNo() = state != STATE_ENABLED

    private fun getOkText() = when (state) {
        STATE_ENABLED -> R.string.ok
        else -> android.R.string.yes
    }

    private fun onOkClicked() = when (state) {
        STATE_ASK -> ScreamAlertDialog(context, STATE_ASK_AGAIN, onScreamEnabled).show()
        STATE_ASK_AGAIN -> ScreamAlertDialog(context, STATE_ENABLED, onScreamEnabled).show()
        else -> onScreamEnabled()
    }

    private fun getMessage() = when (state) {
        STATE_ASK -> R.string.want_to_scream
        STATE_ASK_AGAIN -> R.string.really_want_to_scream
        else -> R.string.scream_now
    }

    companion object {
        const val STATE_ASK = 0
        const val STATE_ASK_AGAIN = 1
        const val STATE_ENABLED = 2
    }
}