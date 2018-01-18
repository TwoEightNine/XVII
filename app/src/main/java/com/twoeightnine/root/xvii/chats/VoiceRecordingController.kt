package com.twoeightnine.root.xvii.chats

import android.media.MediaRecorder
import android.os.CountDownTimer
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.twoeightnine.root.xvii.utils.secToTime
import java.io.File
import java.io.IOException

/**
 * Created by twoeightnine on 1/18/18.
 */

class VoiceRecordingController(private val rlRecording: RelativeLayout,
                               private val tvTimer: TextView,
                               private val storeDir: File,
                               private val onRecorded: (String) -> Unit,
                               private val onError: (String) -> Unit) {

    private var recorder: MediaRecorder? = null
    private val fileName = File(storeDir, RECORD_NAME).absolutePath
    private val timer = RecordTimer()

    fun startRecording() {
        rlRecording.visibility = View.VISIBLE
        timer.start()
        recorder = MediaRecorder()
        if (recorder != null) {
            recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            recorder!!.setOutputFile(fileName)
            try {
                recorder!!.prepare()
                recorder!!.start()
            } catch (e: IOException) {
                onError.invoke(e.message ?: "null")
            }
        }
    }

    fun stopRecording() {
        rlRecording.visibility = View.GONE
        timer.cancel()
        try {
            if (recorder != null) {
                recorder!!.stop()
                recorder!!.release()
                recorder = null
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            recorder = null
        }
        if (timer.lastDuration >= RECORD_MIN_DURATION) {
            onRecorded.invoke(fileName)
        }
    }

    companion object {
        private val RECORD_NAME = "voice.amr"
        private val RECORD_MIN_DURATION = 1
        private val IMPLICIT_DURATION = 1000000L
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
            tvTimer.text = secToTime(spent)
        }
    }
}