package global.msnthrp.xvii.core.crypto.algorithm

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


object Pbkdf2HmacSha1 {

    private const val ITERS = 1000
    private const val KEY_LENGTH = 256 // 32 bytes
    private val SALT = "9w04bescyxfon37tcx395vwn".toByteArray()

    fun deriveFromKey(userKey: String): ByteArray {
        val spec = PBEKeySpec(userKey.toCharArray(), SALT, ITERS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return skf.generateSecret(spec).encoded
    }
}