package global.msnthrp.xvii.core.crypto.engine

import java.io.File

interface CryptoEngineFileSource {

    fun readFromFile(file: File): ByteArray?

    fun writeToFile(fileName: String, bytes: ByteArray): File?

}