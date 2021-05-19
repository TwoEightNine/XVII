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

import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.activities.ExceptionActivity
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.lg.TextEventTransformer
import com.twoeightnine.root.xvii.managers.Prefs

class ExceptionHandler(private var context: Context): Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val errorReport = ReportTool()
                .addStacktrace(e)
                .addDeviceInfo()
                .addLogs(L.events(TextEventTransformer(), COUNT))
                .addPrefs(Prefs.getSettings())
                .toString()

        try {
            val intent = Intent(context, ExceptionActivity::class.java)
            intent.putExtra(ExceptionActivity.ERROR, errorReport)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }

    companion object {
        const val COUNT = 60
    }
}