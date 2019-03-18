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
        serialized ?: return null

        return gson.fromJson<LongPollServer>(serialized, LongPollServer::class.java)
    }

    companion object {
        const val NAME = "longPoll"

        const val SERVER = "server"

    }
}