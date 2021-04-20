package global.msnthrp.xvii.core.crypto.algorithm

import global.msnthrp.xvii.core.crypto.CryptoUtils
import junit.framework.Assert.assertEquals
import org.junit.Test


class KdfTest {

    @Test
    fun kdf_sameHash() {
        val hash1 = Pbkdf2HmacSha1.deriveFromKey(USER_KEY)
        val hash2 = Pbkdf2HmacSha1.deriveFromKey(USER_KEY)
        assertEquals(CryptoUtils.bytesToHex(hash1), CryptoUtils.bytesToHex(hash2))
    }

    companion object {

        const val USER_KEY = "someUserKey"
    }
}