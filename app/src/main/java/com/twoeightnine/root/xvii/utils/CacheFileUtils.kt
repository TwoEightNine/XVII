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

package com.twoeightnine.root.xvii.utils

import android.content.Context
import com.twoeightnine.root.xvii.lg.L
import java.io.File

object CacheFileUtils {

    const val DIR_GALLERY = "gallery_cache"
    const val DIR_NOTIFICATIONS = "notifications_cache"
    const val DIR_FILES = "files"
    const val DIR_CROPPED = "cropped"
    private const val DIR_PICASSO = "picasso-cache"
    private const val DIR_IMAGE_MANAGER = "image_manager_disk_cache"
    private const val DIR_WEB_VIEW = "WebView"

    const val FILE_VOICE_AMR = "voice.amr"
    const val FILE_VOICE_WAV = "voice.wav"
    const val FILE_SHARE = "share.jpg"
    const val FILE_RICH_CONTENT = "richContent.gif"

    private val dirs = listOf(
            DIR_GALLERY,
            DIR_NOTIFICATIONS,
            DIR_PICASSO,
            DIR_IMAGE_MANAGER,
            DIR_WEB_VIEW,
            DIR_FILES,
            DIR_CROPPED
    )

    private val files = listOf(
            FILE_VOICE_AMR,
            FILE_VOICE_WAV,
            FILE_SHARE,
            FILE_RICH_CONTENT,
    )

    fun getCacheSize(context: Context?): Long {
        val cacheDir = context?.cacheDir ?: return 0

        var size = 0L
        // dirs
        dirs.forEach { dir ->
            size += getSizeOfDir(File(cacheDir, dir))
        }
        files.forEach { file ->
            size += File(cacheDir, file).length()
        }
        return size
    }

    fun clearCache(context: Context?) {
        val cacheDir = context?.cacheDir ?: return

        dirs.forEach { dir ->
            emptyDir(File(cacheDir, dir))
        }
        files.forEach { file ->
            File(cacheDir, file).delete()
        }
    }

    private fun getSizeOfDir(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                getSizeOfDir(file)
            } else {
                file.length()
            }
        }
        return size
    }

    private fun emptyDir(dir: File) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                emptyDir(file)
            } else {
                file.delete()
            }
        }
    }

    /**
     * temporary fix to clean filesDir
     */
    fun deleteFilesCompat(context: Context, exceptPaths: List<String>) {
        val tag = "files compat"
        try {
            var deletedCount = 0
            var deletedSize = 0L
            context.filesDir.listFiles()?.forEach { file ->
                if (file.exists()) {
                    val size = file.length()
                    if (file.absolutePath !in exceptPaths) {
                        file.delete()

                        deletedCount += 1
                        deletedSize += size
                    }
                }
            }
            L.tag(tag).log("deleted $deletedCount files, released $deletedSize bytes")
        } catch (e: Exception) {
            L.tag(tag).throwable(e)
        }
    }

}