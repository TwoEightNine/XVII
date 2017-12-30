package com.twoeightnine.root.xvii.background

import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.SystemClock
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import java.io.IOException

/**
 * 3/13/16 i wrote this long before i got hired
 */

class MediaPlayerAsyncTask(private val listener: (() -> Unit)?) : AsyncTask<String, Void, Void?>() {

    lateinit private var player: MediaPlayer
    lateinit private var url: String

    var isExecuting = false
        private set
    private var time = 0

    override fun onPreExecute() {
        super.onPreExecute()
        player = MediaPlayer()
    }

    override fun doInBackground(vararg strings: String): Void? {
        isExecuting = true
        url = strings[0]
        if (Prefs.playerUrl == url) {
            time = Prefs.playerTime
        }
        writeURL()
        Lg.i("PLAYER from time $time playing ${getUrl()}")
        try {
            player.setDataSource(url)
            player.prepare()
        } catch (e: IOException) {
            Lg.wtf("PLAYER error: ${e.message} with URL = ${getUrl()}")
            return null
        }

        player.start()
        if (time != 0) {
            player.seekTo(time)
        }
        while (player.isPlaying && !isCancelled) {
            SystemClock.sleep(200)
            time += 200
        }
        Lg.i("PLAYER done")
        stopPlayer()
        if (!isCancelled) {
            time = 0
        }
        writeTime()
        return null
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        listener?.invoke()
    }

    private fun stopPlayer() {
        player.stop()
        isExecuting = false
        Lg.i("PLAYER stopped")
    }

    private fun writeURL() {
        Prefs.playerUrl = url
    }

    private fun getUrl() = if (BuildConfig.DEBUG) url else if (url.isNotEmpty()) "nonEmptyUrl" else "emptyUrl"

    private fun writeTime() {
        Prefs.playerTime = time
    }

    companion object {
        var URL = "URL"
        var TIME = "time"
    }
}
