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

package global.msnthrp.xvii.data.crypto.engine

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import global.msnthrp.xvii.core.crypto.engine.CryptoEngineEncoder
import global.msnthrp.xvii.core.crypto.engine.CryptoEngineRepo

class PreferencesCryptoEngineRepo(
        context: Context,
        private val cryptoEngineEncoder: CryptoEngineEncoder
) : CryptoEngineRepo {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    override fun getKeyOrNull(peerId: Int): ByteArray? {
        val prefKey = getPeerKey(peerId)
        return if (preferences.contains(prefKey)) {
            try {
                cryptoEngineEncoder.decode(preferences.getString(getPeerKey(peerId), null) ?: "")
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    override fun setKey(peerId: Int, key: ByteArray) {
        preferences.edit {
            putString(getPeerKey(peerId), cryptoEngineEncoder.encode(key))
        }
    }

    override fun clearAll() {
        preferences.edit {
            clear()
        }
    }

    private fun getPeerKey(peerId: Int) = "peer$peerId"

    companion object {
        private const val NAME = "cryptoStorage"
    }
}