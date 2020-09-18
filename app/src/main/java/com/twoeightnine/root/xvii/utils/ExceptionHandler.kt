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