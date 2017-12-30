package com.twoeightnine.root.xvii.utils

import android.os.AsyncTask
import android.os.Environment
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.managers.Lg
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadFileAsyncTask(private val refresh: Boolean = false,
                            private val noGallery: Boolean = false) : AsyncTask<String, Void, Boolean>() {
    private var url: String? = null
    var listener: ((String) -> Unit)? = null
    private var absolutePath: String? = null

    override fun doInBackground(vararg strings: String): Boolean? {
        try {
            this.url = strings[0]
            val url = URL(strings[0])
            val fileName = strings[1]
            val dir = strings[2]
            var path = DEFAULT_PATH
            when (dir) {
                STI -> path = STICKER_PATH
                PIC -> path = PICTURES_PATH
            }

            val destDir = Environment.getExternalStoragePublicDirectory(path)
            destDir.mkdir()
            val urlForLog = if (BuildConfig.DEBUG) url.toString() else "someUrl"
            val pathForLog = if (BuildConfig.DEBUG) destDir.absolutePath else "somePath"
            Lg.i("downloading $urlForLog into $pathForLog")
            val file = File(destDir, fileName)
            absolutePath = file.absolutePath
            if (file.exists() && !refresh) {
                return true
            }

            val httpCon = url.openConnection() as HttpURLConnection
            httpCon.requestMethod = "GET"
            httpCon.connect()

            if (httpCon.responseCode == HttpURLConnection.HTTP_OK) {
                val input = httpCon.inputStream
                val fos = FileOutputStream(file)
                fos.write(streamToBytes(input))
                fos.close()
                input.close()
                httpCon.disconnect()
            }
            return true

        } catch (e: Exception) {
            Lg.wtf("downloading error: ${e.message}")
            e.printStackTrace()
        }

        return false
    }

    override fun onPostExecute(aBoolean: Boolean?) {
        super.onPostExecute(aBoolean)
        val result = if (!noGallery && absolutePath != null) absolutePath!! else ""
        listener?.invoke(result)
    }

    companion object {

        val PIC = "pic"
        val AUD = "aud"
        val STI = "sti"
        val DEFAULT_PATH = Environment.DIRECTORY_DOWNLOADS + "/vk"
        val STICKER_PATH = Environment.DIRECTORY_DOWNLOADS + "/stickers"
        val PICTURES_PATH = Environment.DIRECTORY_PICTURES + "/vk"
    }
}