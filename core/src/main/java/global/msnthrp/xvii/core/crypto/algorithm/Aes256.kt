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

import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Aes256 {

    private const val KEY_LENGTH = 32
    private const val IV_LENGTH = 16
    private const val CIPHER = "AES/CBC/PKCS5Padding"
    private const val AES = "AES"
    private const val KEY_LENGTH_EXCEPTION = "AES-256 requires $KEY_LENGTH byte of key"
    private const val IV_LENGTH_EXCEPTION = "AES-256 requires $IV_LENGTH byte of IV"

    @Throws(java.io.UnsupportedEncodingException::class, NoSuchAlgorithmException::class,
            NoSuchPaddingException::class, InvalidKeyException::class,
            InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class,
            BadPaddingException::class)
    fun encrypt(ivBytes: ByteArray, keyBytes: ByteArray, textBytes: ByteArray): ByteArray {

        if (keyBytes.size != KEY_LENGTH) {
            throw InvalidKeyException(KEY_LENGTH_EXCEPTION)
        }
        if (ivBytes.size != IV_LENGTH) {
            throw InvalidKeyException(IV_LENGTH_EXCEPTION)
        }
        val ivSpec = IvParameterSpec(ivBytes)
        val newKey = SecretKeySpec(keyBytes, AES)
        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec)
        return cipher.doFinal(textBytes)
    }

    @Throws(java.io.UnsupportedEncodingException::class, NoSuchAlgorithmException::class,
            NoSuchPaddingException::class, InvalidKeyException::class,
            InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class,
            BadPaddingException::class)
    fun decrypt(ivBytes: ByteArray, keyBytes: ByteArray, textBytes: ByteArray): ByteArray {

        if (keyBytes.size != KEY_LENGTH) {
            throw InvalidKeyException(KEY_LENGTH_EXCEPTION)
        }
        if (ivBytes.size != IV_LENGTH) {
            throw InvalidKeyException(IV_LENGTH_EXCEPTION)
        }
        val ivSpec = IvParameterSpec(ivBytes)
        val newKey = SecretKeySpec(keyBytes, AES)
        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec)
        return cipher.doFinal(textBytes)
    }


}
