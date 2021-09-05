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
import java.io.File

const val DIR_GALLERY = "gallery_cache"
const val DIR_NOTIFICATIONS = "notifications_cache"
const val DIR_PICASSO = "picasso-cache"
const val DIR_IMAGE_MANAGER = "image_manager_disk_cache"
const val DIR_WEB_VIEW = "WebView"

const val FILE_VOICE = "voice.amr"
const val FILE_VOICE_2 = "voice.wav"
const val FILE_SHARE = "share.jpg"
const val FILE_RICH_CONTENT = "richContent.gif"


fun getCacheSize(context: Context?): Long {
    val cacheDir = context?.cacheDir ?: return 0

    var size = 0L
    // dirs
    size += getSizeOfDir(File(cacheDir, DIR_GALLERY))
    size += getSizeOfDir(File(cacheDir, DIR_PICASSO))
    size += getSizeOfDir(File(cacheDir, DIR_NOTIFICATIONS))
    size += getSizeOfDir(File(cacheDir, DIR_IMAGE_MANAGER))
    size += getSizeOfDir(File(cacheDir, DIR_WEB_VIEW))
    // files
    size += File(cacheDir, FILE_VOICE).length()
    size += File(cacheDir, FILE_VOICE_2).length()
    size += File(cacheDir, FILE_SHARE).length()
    size += File(cacheDir, FILE_RICH_CONTENT).length()

    return size
}

fun clearCache(context: Context?) {
    val cacheDir = context?.cacheDir ?: return

    emptyDir(File(cacheDir, DIR_GALLERY))
    emptyDir(File(cacheDir, DIR_PICASSO))
    emptyDir(File(cacheDir, DIR_NOTIFICATIONS))
    emptyDir(File(cacheDir, DIR_IMAGE_MANAGER))
    emptyDir(File(cacheDir, DIR_WEB_VIEW))

    File(cacheDir, FILE_VOICE).delete()
    File(cacheDir, FILE_VOICE_2).delete()
    File(cacheDir, FILE_SHARE).delete()
    File(cacheDir, FILE_RICH_CONTENT).delete()
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