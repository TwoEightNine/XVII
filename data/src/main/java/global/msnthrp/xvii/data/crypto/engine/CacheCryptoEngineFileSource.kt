package global.msnthrp.xvii.data.crypto.engine

import android.content.Context
import global.msnthrp.xvii.core.crypto.engine.CryptoEngineFileSource
import global.msnthrp.xvii.data.utils.FileUtils
import java.io.File

class CacheCryptoEngineFileSource(context: Context) : CryptoEngineFileSource {

    private val cacheDir = context.cacheDir

    override fun readFromFile(file: File): ByteArray? =
            FileUtils.getBytesFromFile(file)

    override fun writeToFile(fileName: String, bytes: ByteArray): File? {
        val file = File(cacheDir, fileName)
        val result = FileUtils.writeBytesToFile(bytes, file)
        return file.takeIf { result }
    }
}