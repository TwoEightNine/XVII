package com.twoeightnine.root.xvii.managers

import android.content.Context
import android.content.SharedPreferences
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.utils.EmojiHelper
import com.twoeightnine.root.xvii.views.emoji.Emoji
import java.util.*

object Prefs {
    
    private val NAME = "prefPref"

    private var PIN = "pin"

    //general
    private val BE_OFFLINE = "beOffline"
    private val MARK = "markAsRead"
    private val TYPING = "typing"
    private val MANUAL_UPD = "manualUpd"

    //notifications
    private val VIBRATE = "vibrate"
    private val SHOW_NOTIF = "showNotif"
    private val SHOW_NOTIF_CHATS = "showNotifChats"
    private val SHOW_NAME = "showName"
    private val SHOW_CONTENT = "showContent"
    private val SOUND = "sound"
    private val MUTE_LIST = "muteList"

    //appearance
    private val NIGHT = "night"
    private val COLOR = "color"
    private val CHAT_BACK = "chatBack"
    private val DEFAULT_COLOR = 0xff8833dd

    //other
    private val RECENT_STICKERS = "recentStickers"
    private val AVAILABLE_STICKERS = "availableStickers"
    private val RECENT_EMOJIS = "recentEmojis"
    private val PLAYER_URL = "playerUrl"
    private val PLAYER_TIME = "playerTime"
    private val CHAT_PASSED = "chatPassed1"
    private val SADBOY = "SADBOY"
    private val COUNT = "count"
    private val LAST_EVENT = "lastEventId"
    private val SHOW_RATE = "showRate"
    private val LAST_STICKERS_UPD = "lastStickersUpd"
    
    private val data: SharedPreferences by lazy {
        App.context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }
    
    var pin: String
        get() = data.getString(PIN, "")
        set(pin) = data.edit().putString(PIN, pin).apply()

    //data
    //general
    var beOffline: Boolean
        get() = data.getBoolean(BE_OFFLINE, true)
        set(beOffline) = data.edit().putBoolean(BE_OFFLINE, beOffline).apply()

    var markAsRead: Boolean
        get() = data.getBoolean(MARK, false)
        set(markAsRead) = data.edit().putBoolean(MARK, markAsRead).apply()

    var showTyping: Boolean
        get() = data.getBoolean(TYPING, false)
        set(showTyping) = data.edit().putBoolean(TYPING, showTyping).apply()

    var manualUpdating: Boolean
        get() = data.getBoolean(MANUAL_UPD, false)
        set(value) = data.edit().putBoolean(MANUAL_UPD, value).apply()

    //notifications
    var vibrate: Boolean
        get() = data.getBoolean(VIBRATE, true)
        set(vibrate) = data.edit().putBoolean(VIBRATE, vibrate).apply()

    var showNotifs: Boolean
        get() = data.getBoolean(SHOW_NOTIF, true)
        set(showNotif) = data.edit().putBoolean(SHOW_NOTIF, showNotif).apply()

    var showNotifsChats: Boolean
        get() = data.getBoolean(SHOW_NOTIF_CHATS, true)
    set(showNotif) = data.edit().putBoolean(SHOW_NOTIF_CHATS, showNotif).apply()

    var showName: Boolean
        get() = data.getBoolean(SHOW_NAME, false)
        set(showName) = data.edit().putBoolean(SHOW_NAME, showName).apply()

    var sound: Boolean
        get() = data.getBoolean(SOUND, false)
        set(sound) = data.edit().putBoolean(SOUND, sound).apply()

    var muteList: MutableList<Int>
        get() {
            val res = ArrayList<Int>()
            val split = data
                    .getString(MUTE_LIST, "")
                    .split(",".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            for (s in split) {
                try {
                    res.add(Integer.parseInt(s))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            return res
        }
        set(mute) {
            val sb = StringBuilder()
            for (id in mute) {
                sb.append(id).append(",")
            }
            data.edit().putString(MUTE_LIST, sb.toString()).apply()
        }

    var showContent: Boolean
        get() = data.getBoolean(SHOW_CONTENT, false)
        set(show) = data.edit().putBoolean(SHOW_CONTENT, show).apply()

    //appearance
    var color: Int
        get() = data.getInt(COLOR, DEFAULT_COLOR.toInt())
        set(value) = data.edit().putInt(COLOR, value).apply()

    var isNight: Boolean
        get() = data.getBoolean(NIGHT, false)
        set(value) = data.edit().putBoolean(NIGHT, value).apply()

    var chatBack: String
        get() = data.getString(CHAT_BACK, "")
        set(value) = data.edit().putString(CHAT_BACK, value).apply()

    //other
    var recentStickers: MutableList<Int>
        get() = data.getString(RECENT_STICKERS, "")
                .split(",")
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
                .toMutableList()
        set(value) = data.edit().putString(RECENT_STICKERS, value.joinToString(separator = ",")).apply()

    var availableStickers: MutableList<Int>
        get() = data.getString(AVAILABLE_STICKERS, "")
                .split(",")
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
                .toMutableList()
        set(value) = data.edit().putString(AVAILABLE_STICKERS, value.joinToString(separator = ",")).apply()

    var playerUrl: String
        get() = data.getString(PLAYER_URL, "")
        set(value) = data.edit().putString(PLAYER_URL, value).apply()

    var playerTime: Int
        get() = data.getInt(PLAYER_TIME, 0)
        set(value) = data.edit().putInt(PLAYER_TIME, value).apply()

    var chatPassed: Boolean
        get() = data.getBoolean(CHAT_PASSED, false)
        set(value) = data.edit().putBoolean(CHAT_PASSED, value).apply()

    var sadBoy: Boolean
        get() = data.getBoolean(SADBOY, false)
        set(value) = data.edit().putBoolean(SADBOY, value).apply()

    var count: Int
        get() = data.getInt(COUNT, 0)
        set(value) = data.edit().putInt(COUNT, value).apply()

    var lastEvent: Int
        get() = data.getInt(LAST_EVENT, 15)
        set(value) = data.edit().putInt(LAST_EVENT, value).apply()

    var showRate: Boolean
        get() = data.getBoolean(SHOW_RATE, true)
        set(value) = data.edit().putBoolean(SHOW_RATE, false).apply()

    var lastStickersUpdate: Int
        get() = data.getInt(LAST_STICKERS_UPD, 0)
        set(value) = data.edit().putInt(LAST_STICKERS_UPD, value).apply()

    var recentEmojis: MutableList<Emoji>
        get() = data.getString(RECENT_EMOJIS, "")
                .split(",")
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
                .map { EmojiHelper.emojis[it] }
                .toMutableList()
        set(value) = data.edit().putString(RECENT_EMOJIS,
                value.map { getPosByEmoji(it) }
                        .joinToString(separator = ",")
                ).apply()

    private fun getPosByEmoji(emoji: Emoji): Int {
        for (pos in EmojiHelper.emojis.indices) {
            if (emoji == EmojiHelper.emojis[pos]) {
                return pos
            }
        }
        return 0
    }

}