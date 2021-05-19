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