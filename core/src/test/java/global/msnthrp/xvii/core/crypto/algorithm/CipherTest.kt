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

class CipherTest {

    @Test
    fun encryption_assert() {
        val enc = Cipher.encrypt(SAMPLE_TEXT, KEY)
        val dec = Cipher.decrypt(enc, KEY)
        assertEquals(dec.verified, true)
        assertEquals(CryptoUtils.bytesToHex(SAMPLE_TEXT), CryptoUtils.bytesToHex(dec.bytes ?: byteArrayOf()))
    }

    @Test
    fun encryption_corruptedData() {
        val enc = Cipher.encrypt(SAMPLE_TEXT, KEY)
        enc[17] = (enc[17] + 1).toByte()
        val dec = Cipher.decrypt(enc, KEY)
        assertEquals(dec.verified, false)
    }

    companion object {

        private val KEY = "qwertyuiqwertyuiqwertyuiqwertyui".toByteArray() //32

        private val SAMPLE_TEXT = """
fun sha256(plain: String) = sha256Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .map { if (it.length == 2) it else "0$" }
        .joinToString(separator = "")

fun bytesToHex(bytes: ByteArray) = bytes
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .map { if (it.length == 2) it else "0" }
        .joinToString(separator = "")

fun getUiFriendlyHash(hash: String) = hash
        .mapIndexed { index, c -> if (index % 2 == 0) c.toString() else "$ " } // spaces
        .mapIndexed { index, s -> if (index % 16 == 15) "$\n" else s } // new-lines
        .map { it.toUpperCase() }
        .joinToString(separator = "")
        """.trimIndent().toByteArray()
    }
}