package com.twoeightnine.root.xvii.lg

import android.util.Log
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.utils.getTime

object Lg {

    private const val TAG = "vktag"
    private const val LOGS_COUNT = 500

    private val logs by lazy { arrayListOf<LgEvent>() }

    fun i(s: String) {
        Log.i(TAG, s)
        logs.add(LgEvent(s))
        truncate()
    }

    fun dbg(s: String) {
        if (BuildConfig.DEBUG) {
            i(s)
        }
    }

    fun wtf(s: String) {
        Log.wtf(TAG, s)
        logs.add(LgEvent(s, LgEvent.Type.ERROR))
        truncate()
    }

    fun getEvents(count: Int = LOGS_COUNT): String {
        val list = if (logs.size > count) logs.drop(logs.size - count) else logs
        return list.map { event ->
            val time = getTime(event.ts, format = "HH:mm:ss")
            val wrap = if (event.type == LgEvent.Type.ERROR) " !! " else ""
            "$wrap$time: ${event.text}$wrap"
        }.joinToString(separator = "\n")
    }

    private fun truncate() {
        if (logs.size > LOGS_COUNT) {
            logs.removeAt(0)
        }
    }

}