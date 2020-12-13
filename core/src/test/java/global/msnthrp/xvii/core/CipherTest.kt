package global.msnthrp.xvii.core

import global.msnthrp.xvii.core.crypto.CryptoUtils
import global.msnthrp.xvii.core.crypto.algorithm.Cipher
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