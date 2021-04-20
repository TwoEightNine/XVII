package global.msnthrp.xvii.data.crypto.engine

import androidx.test.runner.AndroidJUnit4
import global.msnthrp.xvii.core.crypto.CryptoUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Base64CryptoEngineEncoderTest {

    private val bytes1 = byteArrayOf(0, 1, 2, 3, 4)
    private val bytes2 = byteArrayOf(-127, -126, -128, 127, 126)
    private val bytes3 = "\\]erewdf}{}{OP@)*#@&^#^%@$^#%@#".toByteArray()

    @Test
    fun encodeDecode_valid() {
        val encoder = Base64CryptoEngineEncoder()
        listOf(bytes1, bytes2, bytes3).forEach { bytes ->
            val encoded = encoder.encode(bytes)
            val decoded = encoder.decode(encoded)
            Assert.assertEquals(CryptoUtils.bytesToHex(bytes), CryptoUtils.bytesToHex(decoded))
        }
    }

}