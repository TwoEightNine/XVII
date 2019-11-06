package com.twoeightnine.root.xvii.utils

import android.content.Context
import java.io.File

const val DIR_GALLERY = "gallery_cache"
const val DIR_NOTIFICATIONS = "notifications_cache"
const val DIR_PICASSO = "picasso-cache"
const val FILE_VOICE = "voice.amr"


fun getCacheSize(context: Context?): Long {
    val cacheDir = context?.cacheDir ?: return 0

    var size = 0L
    // dirs
    size += getSizeOfDir(File(cacheDir, DIR_GALLERY))
    size += getSizeOfDir(File(cacheDir, DIR_PICASSO))
    size += getSizeOfDir(File(cacheDir, DIR_NOTIFICATIONS))
    // files
    size += File(cacheDir, FILE_VOICE).length()

    return size
}

fun clearCache(context: Context?) {
    val cacheDir = context?.cacheDir ?: return

    emptyDir(File(cacheDir, DIR_GALLERY))
    emptyDir(File(cacheDir, DIR_PICASSO))
    emptyDir(File(cacheDir, DIR_NOTIFICATIONS))

    File(cacheDir, FILE_VOICE).delete()
}

private fun getSizeOfDir(dir: File): Long {
    var size = 0L
    dir.listFiles().forEach { file ->
        size += file.length()
    }
    return size
}

private fun emptyDir(dir: File) {
    dir.listFiles().forEach { it.delete() }
}