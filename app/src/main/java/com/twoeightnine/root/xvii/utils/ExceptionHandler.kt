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

    private val lineSeparator = "\n"

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val stackTrace = StringWriter()
        e?.printStackTrace(PrintWriter(stackTrace))
        val errorReport = StringBuilder()
                .append("************** CAUSE OF ERROR *************\n\n")
                .append(stackTrace.toString())
                .append("\n******************* LOGS ******************\n")
        
        val logs = Lg.logs
        val start = if (logs.size < 30) 0 else logs.size - 30
        errorReport.append(logs.subList(start, logs.size).joinToString(separator = "\n"))
                .append("\n\n************ DEVICE INFORMATION ***********\n")
                .append("RAM: ")
                .append(getTotalRAM())
                .append(lineSeparator)
                .append("Version: ")
                .append(BuildConfig.VERSION_NAME)
                .append(lineSeparator)
                .append("Brand: ")
                .append(Build.MANUFACTURER)
                .append(lineSeparator)
                .append("Model: ")
                .append(Build.MODEL)
                .append(lineSeparator)
                .append("SDK: ")
                .append(Build.VERSION.SDK)
                .append(lineSeparator)

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
}