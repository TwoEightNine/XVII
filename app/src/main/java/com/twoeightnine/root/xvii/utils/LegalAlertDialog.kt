/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs

class LegalAlertDialog(context: Context) : AlertDialog(context) {

    init {
        setCancelable(false)
        setMessage(LegalLinksUtils.formatLegalText(context, R.string.alert_privacy_and_tos))

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.alert_privacy_and_tos_agree)) { _, _ ->
            Prefs.legalAccepted = true
        }
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.alert_privacy_and_tos_disagree)) { _, _ ->
            (context as? Activity)?.finishAffinity()
        }
    }

    fun showIfNotAccepted() {
        if (!Prefs.legalAccepted) {
            show()
            stylize()
        }
    }

    override fun show() {
        super.show()
        findViewById<TextView>(android.R.id.message)?.apply {
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(ContextCompat.getColor(context, R.color.link_color))
        }
    }
}