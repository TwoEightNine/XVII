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

import androidx.test.runner.AndroidJUnit4
import global.msnthrp.xvii.core.crypto.CryptoUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Base64CryptoEngineEncoderTest {

    private val bytes1 = byteArrayOf(0, 1, 2, 3, 4)
    private val bytes2 = byteArrayOf(-127, -126, -128, 127, 126)
    private val bytes3 = "\\]erewdf}{}{OP@)*#@&^#^%@$^#%@#".toByteArray()

    @Test
    fun encodeDecode_valid() {
        val encoder = Base64CryptoEngineEncoder()
        listOf(bytes1, bytes2, bytes3).forEach { bytes ->
            val encoded = encoder.encode(bytes)
            val decoded = encoder.decode(encoded)
            Assert.assertEquals(CryptoUtils.bytesToHex(bytes), CryptoUtils.bytesToHex(decoded))
        }
    }

}