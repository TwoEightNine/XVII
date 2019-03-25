package com.twoeightnine.root.xvii.utils

import android.content.Context
import com.twoeightnine.root.xvii.lg.Lg

class StatTool(context: Context) {

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    private var launches: Int
        get() = prefs.getInt(LAUNCHES, 0)
        set(value) = prefs.edit().putInt(LAUNCHES, value).apply()

//    private var

    fun incLaunch() {
        try {
            launches++
        } catch (e: Exception) {
            lw("incLaunch ${e.message}")
        }
    }

    private fun lw(s: String) {
        Lg.wtf("[stat] $s")
    }

    companion object {

        private const val NAME = "statPrefs"

        private const val LAUNCHES = "launches"

        private var instance: StatTool? = null

        fun get() = instance

        fun init(context: Context) {
            instance ?: synchronized(this) {
                instance ?: StatTool(context).also { instance = it }
            }
        }
    }
}