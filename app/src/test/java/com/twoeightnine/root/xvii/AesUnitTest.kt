package com.twoeightnine.root.xvii

import com.twoeightnine.root.xvii.crypto.bytesToHex
import com.twoeightnine.root.xvii.crypto.cipher.Aes256
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.security.InvalidKeyException

/**
 * Created by twoeightnine on 1/25/18.
 */
class AesUnitTest {

    @Rule @JvmField
    val thrown = ExpectedException.none()

    @Test
    fun encrypting_invalidKeyLength() {
        thrown.expect(InvalidKeyException::class.java)
        Aes256.encrypt(IV_VALID, KEY_INVALID, TEXT_EXAMPLE)
    }

    @Test
    fun decrypting_invalidKeyLength() {
        thrown.expect(InvalidKeyException::class.java)
        Aes256.decrypt(IV_VALID, KEY_INVALID, TEXT_EXAMPLE)
    }

    @Test
    fun encrypting_invalidIvLength() {
        thrown.expect(InvalidKeyException::class.java)
        Aes256.encrypt(IV_INVALID, KEY_VALID, TEXT_EXAMPLE)
    }

    @Test
    fun decrypting_invalidIvLength() {
        thrown.expect(InvalidKeyException::class.java)
        Aes256.decrypt(IV_INVALID, KEY_VALID, TEXT_EXAMPLE)
    }

    @Test
    fun encrypting_isCorrect() {
        val encrypted = Aes256.encrypt(IV_VALID, KEY_VALID, TEXT_EXAMPLE)
        val decrypted = Aes256.decrypt(IV_VALID, KEY_VALID, encrypted)
        assertEquals(bytesToHex(decrypted), bytesToHex(TEXT_EXAMPLE))
    }

    companion object {
        val KEY_VALID = "qwertyuiqwertyuiqwertyuiqwertyui".toByteArray() //32
        val KEY_INVALID = "qwertyuiqwertyui".toByteArray() //16

        val IV_VALID = "qwertyuiqwertyui".toByteArray() // 16
        val IV_INVALID = "qwertyuiqwertyu".toByteArray() //15

        val TEXT_EXAMPLE = """
            on va pas se laisser abattre
            pour allah on veut mourir
            on va rester pour combattre
            partir avec le sourire

            l'equipé toka ish ish
            algier paris baghdad
        """.toByteArray()
    }

}