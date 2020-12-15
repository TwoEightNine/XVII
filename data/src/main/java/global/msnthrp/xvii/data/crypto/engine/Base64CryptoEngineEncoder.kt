package global.msnthrp.xvii.data.crypto.engine

import android.util.Base64
import global.msnthrp.xvii.core.crypto.engine.CryptoEngineEncoder

class Base64CryptoEngineEncoder : CryptoEngineEncoder {

    override fun encode(bytes: ByteArray): String =
            Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)

    override fun decode(string: String): ByteArray =
            Base64.decode(string, Base64.NO_WRAP or Base64.URL_SAFE)

}