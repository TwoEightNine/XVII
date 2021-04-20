package global.msnthrp.xvii.core.crypto.engine

interface CryptoEngineRepo {

    fun getKeyOrNull(peerId: Int): ByteArray?

    fun setKey(peerId: Int, key: ByteArray)

    fun clearAll()

}