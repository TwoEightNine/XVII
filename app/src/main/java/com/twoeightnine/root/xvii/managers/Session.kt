package com.twoeightnine.root.xvii.managers

import android.content.Context
import android.content.SharedPreferences
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.utils.time

@Deprecated("Will be replaced later")
object Session {

    private const val NAME = "sessionPref"

    private const val PIN_LAST_PROMPT = "pinLastPrompt"
    private const val PIN_LAST_FAILED_PROMPT = "pinLastFailedPrompt"
    private const val PIN_BRUTE_FORCED = "pinBruteForced"

    private const val PIN_THRESHOLD = 20
    private const val PIN_RETRY_THRESHOLD = 60

    private val pref: SharedPreferences by lazy {
        App.context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var pinLastPromptResult: Int
        get() = pref.getInt(PIN_LAST_PROMPT, 0)
        set(value) = pref.edit().putInt(PIN_LAST_PROMPT, value).apply()

    var pinBruteForced: Boolean
        get() = pref.getBoolean(PIN_BRUTE_FORCED, false)
        set(value) = pref.edit().putBoolean(PIN_BRUTE_FORCED, value).apply()

    var pinLastFailedPrompt: Int
        get() = pref.getInt(PIN_LAST_FAILED_PROMPT, 0)
        set(value) = pref.edit().putInt(PIN_LAST_FAILED_PROMPT, value).apply()

    fun needToPromptPin() = Prefs.pin.isNotEmpty() && time() - pinLastPromptResult > PIN_THRESHOLD

    fun needToWaitAfterFailedPin() = pinBruteForced && Prefs.pin.isNotEmpty()
            && time() - pinLastFailedPrompt <= PIN_RETRY_THRESHOLD

    fun clearAll() {
        pref.edit().clear().apply()
    }
}