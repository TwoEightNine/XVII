package com.twoeightnine.root.xvii.managers

import android.content.Context
import android.content.SharedPreferences
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.model.LongPollServer
import com.twoeightnine.root.xvii.utils.time

object Session {

    private val NAME = "sessionPref"

    private val TOKEN = "token"
    private val UID = "uid"
    private val FULLNAME = "fullname"
    private val PHOTO = "photo"

    private val SERVER = "server"
    private val KEY = "key"
    private val TS = "ts"
    private val COOKIES = "cookies"
    private val SERVICE_LAST_ACTION = "serviceLastAction"

    private val activeTimeThreshold = 30

    private val pref: SharedPreferences by lazy {
        App.context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var token
        get() = pref.getString(TOKEN, "")
        set(value) {
            pref.edit().putString(TOKEN, value).apply()
        }

    var uid
        get() = pref.getInt(UID, 0)
        set(value) {
            pref.edit().putInt(UID, value).apply()
        }

    var fullName
        get() = pref.getString(FULLNAME, "")
        set(value) {
            pref.edit().putString(FULLNAME, value).apply()
        }

    var photo
        get() = pref.getString(PHOTO, "err")
        set(value) {
            pref.edit().putString(PHOTO, value).apply()
        }

    var longPoll
        get() = LongPollServer(
                pref.getString(KEY, ""),
                pref.getString(SERVER, ""),
                pref.getInt(TS, 0)
        )
        set(value) {
            pref.edit().putString(KEY, value.key).apply()
            pref.edit().putString(SERVER, value.server).apply()
            pref.edit().putInt(TS, value.ts).apply()
        }

    var timeStamp
        get() = pref.getInt(TS, 0)
        set(value) {
            pref.edit().putInt(TS, value).apply()
        }

    var musicCookie
        get() = pref.getStringSet(COOKIES, HashSet<String>())
        set(value) = pref.edit().putStringSet(COOKIES, value).apply()

    var serviceLastAction
        get() = pref.getInt(SERVICE_LAST_ACTION, 0)
        set (value) {
            pref.edit().putInt(SERVICE_LAST_ACTION, value).apply()
        }

    fun isActive() = time() - serviceLastAction < activeTimeThreshold
}