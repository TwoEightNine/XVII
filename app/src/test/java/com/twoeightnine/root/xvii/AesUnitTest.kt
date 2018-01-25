package com.twoeightnine.root.xvii

import com.twoeightnine.root.xvii.utils.crypto.AES256Cipher
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.security.InvalidKeyException
import java.util.*

/**
 * Created by twoeightnine on 1/25/18.
 */
class AesUnitTest {

    @Rule @JvmField
    val thrown = ExpectedException.none()

    @Test
    fun encrypting_invalidKeyLength() {
        thrown.expect(InvalidKeyException::class.java)
        AES256Cipher.encrypt(IV_VALID, KEY_INVALID, TEXT_EXAMPLE)
    }

    @Test
    fun decrypting_invalidKeyLength() {
        thrown.expect(InvalidKeyException::class.java)
        AES256Cipher.decrypt(IV_VALID, KEY_INVALID, TEXT_EXAMPLE)
    }

    @Test
    fun encrypting_invalidIvLength() {
        thrown.expect(InvalidKeyException::class.java)
        AES256Cipher.encrypt(IV_INVALID, KEY_VALID, TEXT_EXAMPLE)
    }

    @Test
    fun decrypting_invalidIvLength() {
        thrown.expect(InvalidKeyException::class.java)
        AES256Cipher.decrypt(IV_INVALID, KEY_VALID, TEXT_EXAMPLE)
    }

//    @Test
    fun encrypting_isCorrect() {
        val encrypted = AES256Cipher.encrypt(IV_VALID, KEY_VALID, TEXT_EXAMPLE)
        val decrypted = AES256Cipher.decrypt(IV_VALID, KEY_VALID, encrypted)
        assert(Arrays.equals(decrypted, TEXT_EXAMPLE))
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