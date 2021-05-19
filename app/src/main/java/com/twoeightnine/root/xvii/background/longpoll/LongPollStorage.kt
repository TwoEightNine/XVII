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

package com.twoeightnine.root.xvii.background.longpoll

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollServer
import javax.inject.Inject

class LongPollStorage @Inject constructor(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    fun saveLongPoll(longPollServer: LongPollServer) {
        val serialized = gson.toJson(longPollServer)
        prefs.edit().putString(SERVER, serialized).apply()
    }

    fun clear() {
        prefs.edit().putString(SERVER, "").apply()
    }

    fun getLongPollServer(): LongPollServer? {
        val serialized = prefs.getString(SERVER, "")
        if (serialized.isNullOrEmpty()) return null

        return gson.fromJson(serialized, LongPollServer::class.java)
    }

    companion object {
        const val NAME = "longPoll"

        const val SERVER = "server"

    }
}