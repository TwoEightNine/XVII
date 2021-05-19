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

package global.msnthrp.xvii.core.crypto

import java.security.MessageDigest
import java.security.SecureRandom

object CryptoUtils {

    fun md5(plain: ByteArray): ByteArray = MessageDigest
            .getInstance("MD5")
            .digest(plain)

    fun sha256(plain: ByteArray): ByteArray = MessageDigest
            .getInstance("SHA-256")
            .digest(plain)

    fun getRandomBytes(numBytes: Int): ByteArray {
        val sr = SecureRandom()
        sr.setSeed(sr.generateSeed(numBytes))
        val bytes = ByteArray(numBytes)
        sr.nextBytes(bytes)
        return bytes
    }

    fun bytesToHex(bytes: ByteArray) = bytes
            .map { Integer.toHexString(it.toInt() and 0xff) }
            .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

    fun hexToBytes(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { i ->
            hex.substring(2 * i, 2 * i + 2).toInt(16).toByte()
        }
    }
}