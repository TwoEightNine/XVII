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
import android.graphics.Color
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.pin.SecurityFragment
import com.twoeightnine.root.xvii.utils.isAndroid10OrHigher
import com.twoeightnine.root.xvii.utils.isMiui
import java.util.*

object Prefs {

    private const val NAME = "prefPref"

    private const val PIN = "pin"
    private const val PIN_MIXTURE_TYPE = "pinMixtureType"
    private const val NOTIFY_ABOUT_INVADERS = "notifyAboutInvadres"
    private const val TAKE_INVADER_PICTURE = "takeInvaderPicture"
    private const val FAKE_APP_TYPE = "fakeAppType"

    //general
    private const val BE_OFFLINE = "beOffline"
    private const val BE_ONLINE = "beOnline"
    private const val HIDE_STATUS = "hideStatus"
    private const val MARK = "markAsRead"
    private const val TYPING = "typing"
    private const val SHOW_SECONDS = "showSeconds"
    private const val LOWER_TEXTS = "lowerTexts"
    private const val APPLE_EMOJIS = "appleEmojis"
    private const val SHOW_STICKERS = "showStickers"
    private const val SHOW_VOICE = "showVoice"
    private const val STORE_CUSTOM_KEYS = "storeCustomKeys"
    private const val SEND_BY_ENTER = "sendByEnter"
    private const val STICKER_SUGGESTIONS = "stickerSuggestions"
    private const val EXACT_SUGGESTIONS = "exactSuggestions"
    private const val JOIN_SHOWN_LAST = "joinShownLast"
    private const val ENABLE_SWIPE_TO_BACK = "enableSwipeToBack"
    private const val LIFT_KEYBOARD_WINDOW = "liftKeyboard"
    private const val SUGGEST_PEOPLE = "suggestPeople"

    //notifications
    private const val SHOW_NOTIF = "showNotif"
    private const val VIBRATE = "vibrate"
    private const val SOUND = "sound"
    private const val SHOW_NAME = "showName"
    private const val SHOW_CONTENT = "showContent"
    private const val LED_COLOR = "ledColor"
    private const val SHOW_NOTIF_CHATS = "showNotifChats"

    private const val VIBRATE_CHATS = "vibrateChats"
    private const val SOUND_CHATS = "soundChats"
    private const val SHOW_CONTENT_CHATS = "showContentChats"
    private const val LED_COLOR_CHATS = "ledColorChats"

    private const val MUTE_LIST = "muteList"

    private const val MENTIONS_YOU = "mentionsYou"
    private const val MENTIONS_ALL = "mentionsAll"
    private const val MENTIONS_ONLINE = "mentionsOnline"

    //appearance
    private const val NIGHT = "night"
    private const val COLOR = "color"
    private const val CHAT_BACK = "chatBack"
    private const val MESSAGE_TEXT_SIZE = "messageTextSize"
    private const val DEFAULT_COLOR = 0xff7b7be2
    private const val USE_STYLED_NOTIFICATIONS = "useStyledNotifications"

    //other
    private const val SHOW_RATE = "showRate"

    private val data: SharedPreferences by lazy {
        App.context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var pin: String
        get() = data.getString(PIN, "") ?: ""
        set(pin) = data.edit().putString(PIN, pin).apply()

    //data
    //general
    var beOffline: Boolean
        get() = data.getBoolean(BE_OFFLINE, false)
        set(beOffline) = data.edit().putBoolean(BE_OFFLINE, beOffline).apply()

    var beOnline: Boolean
        get() = data.getBoolean(BE_ONLINE, false)
        set(value) = data.edit().putBoolean(BE_ONLINE, value).apply()

    var hideStatus: Boolean
        get() = data.getBoolean(HIDE_STATUS, false)
        set(markAsRead) = data.edit().putBoolean(HIDE_STATUS, markAsRead).apply()

    var markAsRead: Boolean
        get() = data.getBoolean(MARK, true)
        set(markAsRead) = data.edit().putBoolean(MARK, markAsRead).apply()

    var showTyping: Boolean
        get() = data.getBoolean(TYPING, true)
        set(showTyping) = data.edit().putBoolean(TYPING, showTyping).apply()

    var showSeconds: Boolean
        get() = data.getBoolean(SHOW_SECONDS, false)
        set(value) = data.edit().putBoolean(SHOW_SECONDS, value).apply()

    var lowerTexts: Boolean
        get() = data.getBoolean(LOWER_TEXTS, true)
        set(value) = data.edit().putBoolean(LOWER_TEXTS, value).apply()

    var appleEmojis: Boolean
        get() = data.getBoolean(APPLE_EMOJIS, false)
        set(value) = data.edit().putBoolean(APPLE_EMOJIS, value).apply()

    var showStickers: Boolean
        get() = data.getBoolean(SHOW_STICKERS, true)
        set(value) = data.edit().putBoolean(SHOW_STICKERS, value).apply()

    var showVoice: Boolean
        get() = data.getBoolean(SHOW_VOICE, true)
        set(value) = data.edit().putBoolean(SHOW_VOICE, value).apply()

    var storeCustomKeys
        get() = data.getBoolean(STORE_CUSTOM_KEYS, true)
        set(value) = data.edit().putBoolean(STORE_CUSTOM_KEYS, value).apply()

    var sendByEnter
        get() = data.getBoolean(SEND_BY_ENTER, false)
        set(value) = data.edit().putBoolean(SEND_BY_ENTER, value).apply()

    var stickerSuggestions
        get() = data.getBoolean(STICKER_SUGGESTIONS, true)
        set(value) = data.edit().putBoolean(STICKER_SUGGESTIONS, value).apply()

    var exactSuggestions
        get() = data.getBoolean(EXACT_SUGGESTIONS, false)
        set(value) = data.edit().putBoolean(EXACT_SUGGESTIONS, value).apply()

    var joinShownLast
        get() = data.getInt(JOIN_SHOWN_LAST, 0)
        set(value) = data.edit().putInt(JOIN_SHOWN_LAST, value).apply()

    var enableSwipeToBack
        get() = data.getBoolean(ENABLE_SWIPE_TO_BACK, !isAndroid10OrHigher())
        set(value) = data.edit().putBoolean(ENABLE_SWIPE_TO_BACK, value).apply()

    var liftKeyboard
        get() = data.getBoolean(LIFT_KEYBOARD_WINDOW, true)
        set(value) = data.edit().putBoolean(LIFT_KEYBOARD_WINDOW, value).apply()

    var suggestPeople: Boolean
        get() = data.getBoolean(SUGGEST_PEOPLE, false)
        set(value) = data.edit().putBoolean(SUGGEST_PEOPLE, value).apply()

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

    var ledColorChats: Int
        get() = data.getInt(LED_COLOR_CHATS, Color.BLACK)
        set(value) = data.edit().putInt(LED_COLOR_CHATS, value).apply()

    var showContentChats: Boolean
        get() = data.getBoolean(SHOW_CONTENT_CHATS, false)
        set(show) = data.edit().putBoolean(SHOW_CONTENT_CHATS, show).apply()

    var muteList: MutableList<Int>
        get() {
            val res = ArrayList<Int>()
            val split = (data.getString(MUTE_LIST, "") ?: "")
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

    var mentionsYou: Boolean
        get() = data.getBoolean(MENTIONS_YOU, true)
        set(show) = data.edit().putBoolean(MENTIONS_YOU, show).apply()

    var mentionsAll: Boolean
        get() = data.getBoolean(MENTIONS_ALL, true)
        set(show) = data.edit().putBoolean(MENTIONS_ALL, show).apply()

    var mentionsOnline: Boolean
        get() = data.getBoolean(MENTIONS_ONLINE, false)
        set(show) = data.edit().putBoolean(MENTIONS_ONLINE, show).apply()


    //appearance
    var color: Int
        get() = data.getInt(COLOR, DEFAULT_COLOR.toInt())
        set(value) = data.edit().putInt(COLOR, value).apply()

    var isLightTheme: Boolean
        get() = data.getBoolean(NIGHT, false)
        set(value) = data.edit().putBoolean(NIGHT, value).apply()

    var useStyledNotifications: Boolean
        get() = data.getBoolean(USE_STYLED_NOTIFICATIONS, !isMiui())
        set(value) = data.edit().putBoolean(USE_STYLED_NOTIFICATIONS, value).apply()

    var chatBack: String
        get() = data.getString(CHAT_BACK, "") ?: ""
        set(value) = data.edit().putString(CHAT_BACK, value).apply()

    var messageTextSize: Int
        get() = data.getInt(MESSAGE_TEXT_SIZE, 15)
        set(value) = data.edit().putInt(MESSAGE_TEXT_SIZE, value).apply()

    //other
    var showRate: Boolean
        get() = data.getBoolean(SHOW_RATE, true)
        set(value) = data.edit().putBoolean(SHOW_RATE, false).apply()

    var notifyAboutInvaders: Boolean
        get() = data.getBoolean(NOTIFY_ABOUT_INVADERS, true)
        set(value) = data.edit().putBoolean(NOTIFY_ABOUT_INVADERS, value).apply()

    var takeInvaderPicture: Boolean
        get() = data.getBoolean(TAKE_INVADER_PICTURE, false)
        set(value) = data.edit().putBoolean(TAKE_INVADER_PICTURE, value).apply()

    var pinMixtureType: SecurityFragment.MixtureType
        get() = data.getString(PIN_MIXTURE_TYPE, null)
                ?.takeIf { it.isNotBlank() }
                ?.let { SecurityFragment.MixtureType.valueOf(it) }
                ?: SecurityFragment.MixtureType.NONE
        set(value) = data.edit().putString(PIN_MIXTURE_TYPE, value.name).apply()

    var fakeAppType: SecurityFragment.FakeAppType
        get() = data.getString(FAKE_APP_TYPE, null)
                ?.takeIf { it.isNotBlank() }
                ?.let { SecurityFragment.FakeAppType.valueOf(it) }
                ?: SecurityFragment.FakeAppType.NONE
        set(value) = data.edit().putString(FAKE_APP_TYPE, value.name).apply()

    fun getSettings() = mapOf<String, Any>(
            BE_OFFLINE to beOffline,
            BE_ONLINE to beOnline,
            STICKER_SUGGESTIONS to stickerSuggestions,
            EXACT_SUGGESTIONS to exactSuggestions,

            SHOW_NOTIF to showNotifs,
            VIBRATE to vibrate,
            SOUND to sound,
            SHOW_NAME to showName,
            SHOW_CONTENT to showContent,

            SHOW_NOTIF_CHATS to showNotifsChats,
            VIBRATE_CHATS to vibrateChats,
            SOUND_CHATS to soundChats,
            SHOW_CONTENT_CHATS to showContentChats,

            "isLightTheme" to isLightTheme,
            COLOR to Integer.toHexString(color),
            USE_STYLED_NOTIFICATIONS to useStyledNotifications,

            "hasPin" to (pin != ""),
            NOTIFY_ABOUT_INVADERS to notifyAboutInvaders,
            TAKE_INVADER_PICTURE to takeInvaderPicture,
            PIN_MIXTURE_TYPE to pinMixtureType,
            FAKE_APP_TYPE to fakeAppType
    )
}