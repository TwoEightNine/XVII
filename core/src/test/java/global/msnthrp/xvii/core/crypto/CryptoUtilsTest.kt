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

import org.junit.Assert
import org.junit.Test

class CryptoUtilsTest {

    private val bytes1 = byteArrayOf(0, 1, 2, 3, 4)
    private val bytes2 = byteArrayOf(16, 17, 23)
    private val bytes3 = "hewn34tut9 8u439 8t34irjci4jpoi13jr0ri8rumx293ri32,x1o2.3d,43th 348urx2j3po,z3..a32z123]e21\\e[3pr0i34092xu87zdyb8237r2731yb8b76t65cFg3nry9cy"
            .toByteArray()

    @Test
    fun sha256_valid() {

        Assert.assertEquals(
                CryptoUtils.bytesToHex(CryptoUtils.sha256(bytes1)),
                "08bb5e5d6eaac1049ede0893d30ed022b1a4d9b5b48db414871f51c9cb35283d"
        )
        Assert.assertEquals(
                CryptoUtils.bytesToHex(CryptoUtils.sha256(bytes2)),
                "acec8a50e1592e119f3c042122d2627170f0a8800410edfd7ec527e7620a417f"
        )
        Assert.assertEquals(
                CryptoUtils.bytesToHex(CryptoUtils.sha256(bytes3)),
                "4d024332f1c4abeeafe4258a614f3bf8079de068b3d505bac53875ad90bcf3d4"
        )
    }

    @Test
    fun bytesToHex_valid() {

        Assert.assertEquals(CryptoUtils.bytesToHex(bytes1), "0001020304")
        Assert.assertEquals(CryptoUtils.bytesToHex(bytes2), "101117")
        Assert.assertEquals(CryptoUtils.bytesToHex(bytes3).take(32), "6865776e333474757439203875343339")
        Assert.assertEquals(CryptoUtils.bytesToHex(bytes3).takeLast(32), "623736743635634667336e7279396379")
    }

    @Test
    fun getRandomBytes_validLength() {
        listOf(1, 5, 16, 32, 289).forEach { numBytes ->
            Assert.assertEquals(CryptoUtils.getRandomBytes(numBytes).size, numBytes)
        }
    }

}