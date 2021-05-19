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

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


object Pbkdf2HmacSha1 {

    private const val ITERS = 1000
    private const val KEY_LENGTH = 256 // 32 bytes
    private val SALT = "9w04bescyxfon37tcx395vwn".toByteArray()

    fun deriveFromKey(userKey: String): ByteArray {
        val spec = PBEKeySpec(userKey.toCharArray(), SALT, ITERS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return skf.generateSecret(spec).encoded
    }
}