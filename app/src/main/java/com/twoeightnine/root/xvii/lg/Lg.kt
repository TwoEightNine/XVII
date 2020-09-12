package com.twoeightnine.root.xvii.lg

import android.util.Log
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.utils.getTime
import java.util.concurrent.locks.ReentrantLock

@Deprecated("Use L instead")
object Lg {

    private const val TAG = "vktag"
    private const val LOGS_COUNT = 500

    private val logs = arrayListOf<LgEvent>()
    private val lock = ReentrantLock()

    fun i(s: String) {
        lock.lock()
        try {
            Log.i(TAG, s)
            logs.add(LgEvent(s))
            truncate()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
    }

    fun dbg(s: String) {
        if (BuildConfig.DEBUG) i(s)
    }

    fun wtf(s: String) {
        lock.lock()
        try {
            Log.wtf(TAG, s)
            logs.add(LgEvent(s, warn = true))
            truncate()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
    }

    fun getEvents(count: Int = LOGS_COUNT): String {
        lock.lock()
        var result = ""
        try {
            val list = if (logs.size > count) logs.drop(logs.size - count) else logs
            result = list.joinToString(separator = "\n") { event ->
                val time = getTime(event.ts, withSeconds = true)
                val wrap = if (event.warn) " !! " else ""
                "$wrap$time: ${event.text}$wrap"
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
        return result
    }

    private fun truncate() {
        lock.lock()
        try {
            if (logs.size > LOGS_COUNT) {
                logs.removeAt(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
    }

}