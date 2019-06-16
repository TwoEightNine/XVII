package com.twoeightnine.root.xvii.managers

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.utils.EmojiHelper
import com.twoeightnine.root.xvii.views.emoji.Emoji
import java.util.*

object Prefs {

    private const val NAME = "prefPref"

    private const val PIN = "pin"

    //general
    private const val BE_OFFLINE = "beOffline"
    private const val MARK = "markAsRead"
    private const val TYPING = "typing"
    private const val MANUAL_UPD = "manualUpd"
    private const val SHOW_SECONDS = "showSeconds"
    private const val LOWER_TEXTS = "lowerTexts"
    private const val APPLE_EMOJIS = "appleEmojis"
    private const val STORE_CUSTOM_KEYS = "storeCustomKeys"

    //notifications
    private const val SHOW_NOTIF = "showNotif"
    private const val VIBRATE = "vibrate"
    private const val SOUND = "sound"
    private const val SHOW_NAME = "showName"
    private const val SHOW_CONTENT = "showContent"
    private const val LED_LIGHTS = "ledLights"
    private const val LED_COLOR = "ledColor"
    private const val SHOW_NOTIF_CHATS = "showNotifChats"

    private const val VIBRATE_CHATS = "vibrateChats"
    private const val SOUND_CHATS = "soundChats"
    private const val SHOW_CONTENT_CHATS = "showContentChats"
    private const val LED_LIGHTS_CHATS = "ledLightsCHats"
    private const val LED_COLOR_CHATS = "ledColorChats"

    private const val MUTE_LIST = "muteList"

    //appearance
    private const val NIGHT = "night"
    private const val COLOR = "color"
    private const val CHAT_BACK = "chatBack"
    private const val DEFAULT_COLOR = 0xff8833dd

    //other
    private const val RECENT_STICKERS = "recentStickers"
    private const val RECENT_EMOJIS = "recentEmojis"
    private const val SHOW_RATE = "showRate"

    private val data: SharedPreferences by lazy {
        App.context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var pin: String
        get() = data.getString(PIN, "")
        set(pin) = data.edit().putString(PIN, pin).apply()

    //data
    //general
    var beOffline: Boolean
        get() = data.getBoolean(BE_OFFLINE, false)
        set(beOffline) = data.edit().putBoolean(BE_OFFLINE, beOffline).apply()

    var markAsRead: Boolean
        get() = data.getBoolean(MARK, true)
        set(markAsRead) = data.edit().putBoolean(MARK, markAsRead).apply()

    var showTyping: Boolean
        get() = data.getBoolean(TYPING, true)
        set(showTyping) = data.edit().putBoolean(TYPING, showTyping).apply()

    var manualUpdating: Boolean
        get() = data.getBoolean(MANUAL_UPD, false)
        set(value) = data.edit().putBoolean(MANUAL_UPD, value).apply()

    var showSeconds: Boolean
        get() = data.getBoolean(SHOW_SECONDS, false)
        set(value) = data.edit().putBoolean(SHOW_SECONDS, value).apply()

    var lowerTexts: Boolean
        get() = data.getBoolean(LOWER_TEXTS, false)
        set(value) = data.edit().putBoolean(LOWER_TEXTS, value).apply()

    var appleEmojis: Boolean
        get() = data.getBoolean(APPLE_EMOJIS, true)
        set(value) = data.edit().putBoolean(APPLE_EMOJIS, value).apply()

    var storeCustomKeys
        get() = data.getBoolean(STORE_CUSTOM_KEYS, true)
        set(value) = data.edit().putBoolean(STORE_CUSTOM_KEYS, value).apply()

    // notifications
    // private
    var showNotifs: Boolean
        get() = data.getBoolean(SHOW_NOTIF, true)
        set(showNotif) = data.edit().putBoolean(SHOW_NOTIF, showNotif).apply()

    var vibrate: Boolean
        get() = data.getBoolean(VIBRATE, true)
        set(vibrate) = data.edit().putBoolean(VIBRATE, vibrate).apply()

    var sound: Boolean
        get() = data.getBoolean(SOUND, false)
        set(sound) = data.edit().putBoolean(SOUND, sound).apply()

    var showName: Boolean
        get() = data.getBoolean(SHOW_NAME, false)
        set(showName) = data.edit().putBoolean(SHOW_NAME, showName).apply()

    var ledLights: Boolean
        get() = data.getBoolean(LED_LIGHTS, false)
        set(ledLights) = data.edit().putBoolean(LED_LIGHTS, ledLights).apply()

    var ledColor: Int
        get() = data.getInt(LED_COLOR, Color.MAGENTA)
        set(value) = data.edit().putInt(LED_COLOR, value).apply()

    var showContent: Boolean
        get() = data.getBoolean(SHOW_CONTENT, false)
        set(show) = data.edit().putBoolean(SHOW_CONTENT, show).apply()

    // notifications
    // other
    var showNotifsChats: Boolean
        get() = data.getBoolean(SHOW_NOTIF_CHATS, true)
        set(showNotif) = data.edit().putBoolean(SHOW_NOTIF_CHATS, showNotif).apply()

    var vibrateChats: Boolean
        get() = data.getBoolean(VIBRATE_CHATS, true)
        set(vibrate) = data.edit().putBoolean(VIBRATE_CHATS, vibrate).apply()

    var soundChats: Boolean
        get() = data.getBoolean(SOUND_CHATS, false)
        set(sound) = data.edit().putBoolean(SOUND_CHATS, sound).apply()

    var ledLightsChats: Boolean
        get() = data.getBoolean(LED_LIGHTS_CHATS, false)
        set(ledLights) = data.edit().putBoolean(LED_LIGHTS_CHATS, ledLights).apply()

    var ledColorChats: Int
        get() = data.getInt(LED_COLOR_CHATS, Color.BLACK)
        set(value) = data.edit().putInt(LED_COLOR_CHATS, value).apply()

    var showContentChats: Boolean
        get() = data.getBoolean(SHOW_CONTENT_CHATS, false)
        set(show) = data.edit().putBoolean(SHOW_CONTENT_CHATS, show).apply()

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

    //appearance
    var color: Int
        get() = data.getInt(COLOR, DEFAULT_COLOR.toInt())
        set(value) = data.edit().putInt(COLOR, value).apply()

    var isLightTheme: Boolean
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

    var showRate: Boolean
        get() = data.getBoolean(SHOW_RATE, true)
        set(value) = data.edit().putBoolean(SHOW_RATE, false).apply()

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