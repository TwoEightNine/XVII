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

package global.msnthrp.xvii.data.longpoll

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import global.msnthrp.xvii.core.longpoll.LongPollServer
import global.msnthrp.xvii.core.longpoll.LongPollServerDataSource
import global.msnthrp.xvii.data.sharedpreferences.SharedPreferencesDelegates

class EncryptedLongPollServerDataSource(context: Context) : LongPollServerDataSource {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs = EncryptedSharedPreferences.create(
            NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override var longPollServer: LongPollServer?
        get() = loadLongPollServer()
        set(value) = saveLongPollServer(value)

    private var key: String? by SharedPreferencesDelegates.StringDelegate(prefs, "key")
    private var server: String? by SharedPreferencesDelegates.StringDelegate(prefs, "server")
    private var ts: Int by SharedPreferencesDelegates.IntDelegate(prefs, "ts")

    private fun loadLongPollServer(): LongPollServer? {
        val key = key ?: return null
        val server = server ?: return null
        val ts = ts
        if (ts == 0) return null

        return LongPollServer(key, server, ts)
    }

    private fun saveLongPollServer(longPollServer: LongPollServer?) {
        key = longPollServer?.key
        server = longPollServer?.server
        ts = longPollServer?.ts ?: 0
    }

    companion object {
        private const val NAME = "longPollServer"
    }
}