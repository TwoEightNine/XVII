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

package com.twoeightnine.root.xvii.lg

import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.ReportTool
import com.twoeightnine.root.xvii.utils.copyToClip
import com.twoeightnine.root.xvii.utils.stylize

class LgAlertDialog(context: Context) : AlertDialog(context) {

    init {
        val events = ReportTool()
                .addDeviceInfo()
                .addLogs(L.events(TextEventTransformer(), COUNT))
                .addPrefs(Prefs.getSettings())
                .toString()
        setMessage(events)
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok)) { _, _ ->
            dismiss()
        }
        setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.copy)) { _, _ ->
            copyToClip(events)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<TextView>(android.R.id.message)?.apply {
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE)
        }
    }

    override fun show() {
        super.show()
        stylize(keepFont = true)
    }

    companion object {
        const val COUNT = 200
        const val FONT_SIZE = 10f
    }

}