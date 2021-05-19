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

package com.twoeightnine.root.xvii.utils

import android.content.Context
import com.twoeightnine.root.xvii.lg.L

class StatTool(context: Context) {

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var launches: Int
        get() = prefs.getInt(LAUNCHES, 0)
        private set(value) = prefs.edit().putInt(LAUNCHES, value).apply()

    private var messagesSent: Int
        get() = prefs.getInt(MESSAGES_SENT, 0)
        set(value) = prefs.edit().putInt(MESSAGES_SENT, value).apply()

    private var messageLenSent: Int
        get() = prefs.getInt(MESSAGE_LEN_SENT, 0)
        set(value) = prefs.edit().putInt(MESSAGE_LEN_SENT, value).apply()

    private var emojisCnt: Int
        get() = prefs.getInt(EMOJIS_CNT, 0)
        set(value) = prefs.edit().putInt(EMOJIS_CNT, value).apply()

    private var stickersSent: Int
        get() = prefs.getInt(STICKERS_SENT, 0)
        set(value) = prefs.edit().putInt(STICKERS_SENT, value).apply()

    private var startTime: Int
        get() = prefs.getInt(START_TIME, 0)
        set(value) = prefs.edit().putInt(START_TIME, value).apply()

    fun incLaunch() {
        try {
            launches++
        } catch (e: Exception) {
            lw("incLaunch", e)
        }
    }

    fun messageSent(message: String) {
        try {
            messagesSent++
            messageLenSent += message.length
            if (EmojiHelper.hasEmojis(message)) {
                emojisCnt++
            }
        } catch (e: Exception) {
            lw("messageSent", e)
        }
    }

    fun stickerSent() {
        try {
            stickersSent++
        } catch (e: Exception) {
            lw("stickersSent", e)
        }
    }

    fun getReport(): String {
        val days = (time() - startTime).toFloat() / 3600 / 24
        val messSent = messagesSent
        val launches = launches

        val messagesSentDaily = if (days != 0f) messSent / days else -1
        val sessionDaily = if (days != 0f) launches / days else -1
        val messageLenAvg = if (messSent != 0) messageLenSent / messSent else -1
        val emojisUsage = if (messSent != 0) emojisCnt / messSent else -1
        val stickersSentDaily = if (days != 0f) stickersSent / days else -1
        return StringBuilder()
                .line("days=$days")
                .line("messagesSentDaily=$messagesSentDaily")
                .line("messageLenAvg=$messageLenAvg")
                .line("emojisUsage=$emojisUsage")
                .line("stickersSentDaily=$stickersSentDaily")
                .line("sessionsDaily=$sessionDaily")
                .toString()
    }

    private fun initStartTime() {
        try {
            if (startTime == 0) {
                startTime = time()
            }
        } catch (e: Exception) {
            lw("init startTime", e)
        }
    }

    private fun StringBuilder.line(s: String) = append(s).append('\n')

    private fun lw(s: String, throwable: Throwable? = null) {
        L.tag("stat").throwable(throwable).log(s)
    }

    companion object {

        private const val NAME = "statPrefs"

        private const val LAUNCHES = "launches"
        private const val MESSAGES_SENT = "messagesSent"
        private const val MESSAGE_LEN_SENT = "messageLenSent"
        private const val EMOJIS_CNT = "emojisCnt"
        private const val STICKERS_SENT = "stickersSent"
        private const val START_TIME = "startTime"

        private var instance: StatTool? = null

        fun get() = instance

        fun init(context: Context) {
            instance ?: synchronized(this) {
                instance ?: StatTool(context).also { instance = it }
            }
            instance?.initStartTime()
        }
    }
}