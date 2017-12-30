package com.twoeightnine.root.xvii.managers

import android.util.Log
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.time

object Lg {

    private val TAG = "vktag"
    private val LOGS_COUNT = 500

    val logs = mutableListOf<String>()

    fun i(s: String) {
        Log.i(TAG, s)
        logs.add("[${getTime(time(), format = "HH:mm:ss")}] $s")
        truncate()
    }

    fun dbg(s: String) {
        if (BuildConfig.DEBUG) {
            i(s)
        }
    }

    fun wtf(s: String) {
        Log.wtf(TAG, s)
        logs.add("!! [${getTime(time(), format = "HH:mm:ss")}] !! $s")
        truncate()
    }

    private fun truncate() {
        if (logs.size > LOGS_COUNT) {
            logs.removeAt(0)
        }
    }

}