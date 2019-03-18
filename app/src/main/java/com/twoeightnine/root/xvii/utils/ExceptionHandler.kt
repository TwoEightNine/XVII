package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.activities.ExceptionActivity
import com.twoeightnine.root.xvii.managers.Lg
import java.io.PrintWriter
import java.io.StringWriter

class ExceptionHandler(private var context: Context): Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val stackTrace = StringWriter()
        e?.printStackTrace(PrintWriter(stackTrace))
        val errorReport = StringBuilder()
                .append("NEW CRASH IN ")
                .append(BuildConfig.VERSION_NAME)
                .append(LINE)
                .append("CAUSE OF ERROR:\n")
                .append(stackTrace.toString())
                .append("\nLOGS:\n")
        
        val logs = Lg.logs
        val start = if (logs.size < 60) 0 else logs.size - 60
        errorReport.append(logs.subList(start, logs.size).joinToString(separator = "\n"))
                .append("\n\nDEVICE INFORMATION:\n")
                .append("RAM: ")
                .append(getTotalRAM())
                .append(LINE)
                .append("Brand: ")
                .append(Build.MANUFACTURER)
                .append(LINE)
                .append("Model: ")
                .append(Build.MODEL)
                .append(LINE)
                .append("SDK: ")
                .append(Build.VERSION.SDK_INT)
                .append(LINE)

        Lg.wtf(errorReport.toString())

        try {
            val intent = Intent(context, ExceptionActivity::class.java)
            intent.putExtra(ExceptionActivity.ERROR, errorReport.toString())
            context.startActivity(intent)
        } catch (e: Exception) {
            Lg.i("error handling exception: ${e.message}")
        }

        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }

    companion object {
        const val LINE = "\n"
    }
}