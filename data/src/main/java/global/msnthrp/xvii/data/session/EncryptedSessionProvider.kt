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

package global.msnthrp.xvii.data.session

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import global.msnthrp.xvii.core.crypto.CryptoUtils
import global.msnthrp.xvii.core.session.SessionProvider
import global.msnthrp.xvii.data.sharedpreferences.SharedPreferencesDelegates
import kotlin.reflect.KProperty


class EncryptedSessionProvider(context: Context) : SessionProvider {

    private var masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private var prefs = EncryptedSharedPreferences.create(
            NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override var token: String? by SharedPreferencesDelegates.StringDelegate(prefs, KEY_TOKEN)

    override var userId: Int by SharedPreferencesDelegates.IntDelegate(prefs, KEY_USER_ID)

    override var fullName: String? by SharedPreferencesDelegates.StringDelegate(prefs, KEY_FULL_NAME)

    override var photo: String? by SharedPreferencesDelegates.StringDelegate(prefs, KEY_PHOTO)

    override var pin: String? by SharedPreferencesDelegates.StringDelegate(prefs, KEY_PIN)

    override val encryptionKey256: ByteArray by EncryptionKeyDelegate()

    override fun clearAll() {
        kotlin.runCatching {
            prefs.edit {
                clear()
            }
        }
    }

    companion object {
        private const val NAME = "session"

        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PHOTO = "photo"
        private const val KEY_PIN = "pin"
        private const val KEY_ENCRYPTION_KEY = "encryption_key"
    }

    private inner class EncryptionKeyDelegate {

        operator fun getValue(thisRef: Any?, prop: KProperty<*>): ByteArray {
            var encryptionKey = prefs.getString(KEY_ENCRYPTION_KEY, null)
                    ?.let(CryptoUtils::hexToBytes)
            if (encryptionKey == null) {
                encryptionKey = CryptoUtils.getRandomBytes(32)
                prefs.edit {
                    putString(KEY_ENCRYPTION_KEY, CryptoUtils.bytesToHex(encryptionKey))
                }
            }
            return encryptionKey
        }

    }
}