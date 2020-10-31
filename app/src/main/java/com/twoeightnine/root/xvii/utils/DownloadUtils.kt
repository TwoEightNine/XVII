package com.twoeightnine.root.xvii.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.twoeightnine.root.xvii.R
import java.io.File

object DownloadUtils {

    fun download(context: Context, file: File, url: String): Long {
        val fileName = file.absolutePath.getUriName()
        val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription(context.getString(R.string.download))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

}