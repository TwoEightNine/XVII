package global.msnthrp.xvii.core.crypto.algorithm

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


object HmacSha256 {

    private const val ALGORITHM = "HmacSHA256"

    fun sign(bytes: ByteArray, key: ByteArray): ByteArray {
        val hmac = Mac.getInstance(ALGORITHM)
        hmac.init(SecretKeySpec(key, ALGORITHM))
        return hmac.doFinal(bytes)
    }
}