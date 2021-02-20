package com.twoeightnine.root.xvii.utils

import android.os.Build
import java.io.*

class ReportTool {

    private val reportSb = StringBuilder()

    fun addStacktrace(throwable: Throwable?) = apply {
        val stackTrace = StringWriter()
        throwable?.printStackTrace(PrintWriter(stackTrace))
        reportSb.append("CAUSE OF ERROR:\n")
                .append(stackTrace.toString())
                .terminate()
    }

    fun addLogs(events: List<String>) = apply {
        reportSb.append("LOGS:\n")
                .append(events.joinToString(separator = "\n"))
                .terminate()
    }

    fun addPrefs(prefs: Map<String, Any>) = apply {
        val prefsStr = prefs.map { "${it.key} = ${it.value}" }
                .joinToString(separator = "\n")
        reportSb.append("PREFS:\n")
                .append(prefsStr)
                .terminate()
    }

    fun addDeviceInfo() = apply {
        reportSb.append("DEVICE INFO:\n")
                .append("${Build.MANUFACTURER} ${Build.MODEL} ")
                .append("(SDK ${Build.VERSION.SDK_INT}, ${getTotalRAM()} RAM)")
                .terminate()
    }

    override fun toString() = reportSb.toString()

    fun toFile(file: File, onWritten: (String) -> Unit) {
        val writer = BufferedWriter(FileWriter(file))
        writer.write(toString())
        writer.close()
        onWritten(file.absolutePath)
    }

    private fun java.lang.StringBuilder.terminate() = append("\n\n")
}