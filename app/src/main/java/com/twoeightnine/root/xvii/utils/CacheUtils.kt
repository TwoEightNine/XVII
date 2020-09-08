package com.twoeightnine.root.xvii.utils

import android.content.Context
import java.io.File

const val DIR_GALLERY = "gallery_cache"
const val DIR_NOTIFICATIONS = "notifications_cache"
const val DIR_PICASSO = "picasso-cache"
const val DIR_IMAGE_MANAGER = "image_manager_disk_cache"
const val DIR_WEB_VIEW = "WebView"

const val FILE_VOICE = "voice.amr"
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