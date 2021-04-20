package global.msnthrp.xvii.data.utils

import java.io.*

object FileUtils {

    fun getBytesFromFile(file: File): ByteArray? {
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return bytes
    }

    fun writeBytesToFile(bytes: ByteArray, file: File): Boolean {
        try {
            val out = FileOutputStream(file.absolutePath)
            out.write(bytes)
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

}