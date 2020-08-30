package com.twoeightnine.root.xvii.managers

import android.content.Context
import android.content.SharedPreferences
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.utils.time

object Session {

    private const val NAME = "sessionPref"

    private const val TOKEN = "token"
    private const val UID = "uid"
    private const val FULL_NAME = "fullname"
    private const val PHOTO = "photo"
    private const val PIN_LAST_PROMPT = "pinLastPrompt"

    private const val PIN_THRESHOLD = 20

    private val pref: SharedPreferences by lazy {
        App.context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var token: String
        get() = pref.getString(TOKEN, "") ?: ""
        set(value) {
            pref.edit().putString(TOKEN, value).apply()
        }

    var uid: Int
        get() = pref.getInt(UID, 0)
        set(value) {
            pref.edit().putInt(UID, value).apply()
        }

    var fullName: String
        get() = pref.getString(FULL_NAME, "") ?: ""
        set(value) {
            pref.edit().putString(FULL_NAME, value).apply()
        }

    var photo: String
        get() = pref.getString(PHOTO, "") ?: ""
        set(value) {
            pref.edit().putString(PHOTO, value).apply()
        }

    var pinLastPromptResult: Int
        get() = pref.getInt(PIN_LAST_PROMPT, 0)
        set(value) = pref.edit().putInt(PIN_LAST_PROMPT, value).apply()

    fun needToPromptPin() = Prefs.pin.isNotEmpty() && time() - pinLastPromptResult > PIN_THRESHOLD
}