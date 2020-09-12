package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.twoeightnine.root.xvii.activities.ExceptionActivity
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.lg.TextEventTransformer
import java.io.PrintWriter
import java.io.StringWriter

class ExceptionHandler(private var context: Context): Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val stackTrace = StringWriter()
        e?.printStackTrace(PrintWriter(stackTrace))
        val errorReport = StringBuilder()
                .append("CAUSE OF ERROR:\n")
                .append(stackTrace.toString())
                .append("\nLOGS:\n")

        errorReport.append(L.events(TextEventTransformer()).takeLast(COUNT).joinToString(separator = "\n"))
                .append("\n\nDEVICE INFORMATION:\n")
                .append("${Build.MANUFACTURER} ${Build.MODEL} ")
                .append("(SDK ${Build.VERSION.SDK_INT}, ${getTotalRAM()} RAM)")

        try {
            val intent = Intent(context, ExceptionActivity::class.java)
            intent.putExtra(ExceptionActivity.ERROR, errorReport.toString())
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