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