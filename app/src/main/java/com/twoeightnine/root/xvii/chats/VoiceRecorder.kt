package com.twoeightnine.root.xvii.chats

import android.content.Context
import android.media.MediaRecorder
import android.os.CountDownTimer
import android.os.Vibrator
import com.twoeightnine.root.xvii.managers.Lg
import java.io.File
import java.io.IOException

/**
 * Created by twoeightnine on 1/18/18.
 */

class VoiceRecorder(
    private val context: Context,
    private val recorderCallback: RecorderCallback
) {

    private var recorder: MediaRecorder? = null
    private val fileName = File(context.cacheDir, RECORD_NAME).absolutePath
    private val timer = RecordTimer()

    fun startRecording() {
        recorderCallback.onVisibilityChanged(true)
        timer.start()
        recorder = MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            setOutputFile(fileName)
            try {
                prepare()
                start()
                vibrate()
            } catch (e: IOException) {
                recorderCallback.onError(e.message ?: "null")
            }
        }
    }

    fun stopRecording(cancelled: Boolean = false) {
        recorderCallback.onVisibilityChanged(false)
        timer.cancel()
        val successfully = try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Lg.i("stop recording: ${e.message}")
            false
        }
        recorder = null
        if (timer.lastDuration >= RECORD_MIN_DURATION && !cancelled && successfully) {
            recorderCallback.onRecorded(fileName)
        }
    }

    private fun vibrate() {
        val vibrate = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrate.vibrate(20L)
    }

    companion object {
        private const val RECORD_NAME = "voice.amr"
        private const val RECORD_MIN_DURATION = 1
        private const val IMPLICIT_DURATION = 60 * 15 * 1000L // 15 minutes
    }

    interface RecorderCallback {
        fun onVisibilityChanged(visible: Boolean)
        fun onTimeUpdated(time: Int)
        fun onRecorded(fileName: String)
        fun onError(error: String)
    }

    private inner class RecordTimer : CountDownTimer(IMPLICIT_DURATION, 1000L) {

        var lastDuration = 0
            private set

        override fun onFinish() {
            stopRecording()
        }

        override fun onTick(millisUntilFinished: Long) {
            val spent = ((IMPLICIT_DURATION - millisUntilFinished) / 1000).toInt()
            lastDuration = spent
            recorderCallback.onTimeUpdated(spent)
        }
    }
}