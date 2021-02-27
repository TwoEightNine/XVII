package global.msnthrp.xvii.core.crypto.algorithm

import global.msnthrp.xvii.core.crypto.CryptoUtils

object Cipher {

    private const val IV_LENGTH = 16
    private const val MAC_LENGTH = 32

    fun encrypt(bytes: ByteArray, key: ByteArray, deterministic: Boolean = false): ByteArray {
        val key1 = CryptoUtils.sha256(key.copyOfRange(0, key.size / 2))
        val key2 = CryptoUtils.sha256(key.copyOfRange(key.size / 2, key.size))

        val iv = when {
            deterministic -> CryptoUtils.md5(key)
            else -> CryptoUtils.getRandomBytes(IV_LENGTH)
        }
        val encrypted = Aes256.encrypt(iv, key1, bytes)
        val mac = HmacSha256.sign(iv + encrypted, key2)
        return iv + encrypted + mac
    }

    fun decrypt(bytes: ByteArray, key: ByteArray): Result {
        if (bytes.size < IV_LENGTH + MAC_LENGTH) return Result(verified = false)

        val key1 = CryptoUtils.sha256(key.copyOfRange(0, key.size / 2))
        val key2 = CryptoUtils.sha256(key.copyOfRange(key.size / 2, key.size))

        val iv = bytes.copyOfRange(0, IV_LENGTH)
        val encrypted = bytes.copyOfRange(IV_LENGTH, bytes.size - MAC_LENGTH)
        val mac = bytes.copyOfRange(bytes.size - MAC_LENGTH, bytes.size)
        if (!mac.contentEquals(HmacSha256.sign(iv + encrypted, key2))) {
            return Result(verified = false)
        }
        val decrypted = Aes256.decrypt(iv, key1, encrypted)
        return Result(decrypted, verified = true)
    }

    class Result(
            val bytes: ByteArray? = null,
            val verified: Boolean = false
    )
}