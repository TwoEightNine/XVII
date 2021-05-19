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

package global.msnthrp.xvii.core.crypto.algorithm

import global.msnthrp.xvii.core.crypto.CryptoUtils
import junit.framework.Assert.assertEquals
import org.junit.Test


class KdfTest {

    @Test
    fun kdf_sameHash() {
        val hash1 = Pbkdf2HmacSha1.deriveFromKey(USER_KEY)
        val hash2 = Pbkdf2HmacSha1.deriveFromKey(USER_KEY)
        assertEquals(CryptoUtils.bytesToHex(hash1), CryptoUtils.bytesToHex(hash2))
    }

    companion object {

        const val USER_KEY = "someUserKey"
    }
}