/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package global.msnthrp.xvii.data.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.*
import java.security.SecureRandom


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

    fun writeToTempFileFromContentUri(context: Context?, uri: Uri): File? {
        context ?: return null

        val fileExtension = getFileExtension(context, uri)
        val dir = File(context.cacheDir, "files")
        dir.mkdir()
        val file = File(dir, "1753${SecureRandom().nextLong()}.$fileExtension")
        val written = writeToFileFromContentUri(context, file, uri)
        return when {
            written -> file
            else -> null
        }
    }

    fun writeToFileFromContentUri(context: Context?, file: File, uri: Uri): Boolean {
        if (context == null) return false
        try {
            val stream = context.contentResolver.openInputStream(uri)
            val output = FileOutputStream(file)
            if (stream == null) return false

            val buffer = ByteArray(4096)
            var read: Int
            while (true) {
                read = stream.read(buffer)
                if (read == -1) break

                output.write(buffer, 0, read)
            }
            output.flush()
            output.close()
            stream.close()
            return true
        } catch (e: java.lang.Exception) {
            // TODO
//            L.def().throwable(e).log("unable to write to file from uri")
        }
        return false
    }


    fun getFileExtension(context: Context, uri: Uri): String? {
        //Check uri format to avoid null
        return if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
    }

}