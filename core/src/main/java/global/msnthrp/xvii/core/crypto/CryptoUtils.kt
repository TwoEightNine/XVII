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
}