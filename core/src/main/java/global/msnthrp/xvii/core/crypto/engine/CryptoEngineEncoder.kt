package global.msnthrp.xvii.core.crypto.engine

interface CryptoEngineEncoder {

    fun encode(bytes: ByteArray): String

    fun decode(string: String): ByteArray
}