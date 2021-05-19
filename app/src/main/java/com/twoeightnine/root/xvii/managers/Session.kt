/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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