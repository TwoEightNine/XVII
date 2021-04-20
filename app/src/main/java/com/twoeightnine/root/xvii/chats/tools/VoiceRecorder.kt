package com.twoeightnine.root.xvii.chats.tools

import android.content.Context
import android.media.MediaRecorder
import android.os.CountDownTimer
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.VibrationHelper
import java.io.File
import kotlin.math.sqrt

/**
 * Created by twoeightnine on 1/18/18.
 */

class VoiceRecorder(
        private val context: Context,
        private val recorderCallback: RecorderCallback
) {

    private var recorder: MediaRecorder? = null
    private val file = File(context.cacheDir, RECORD_NAME)
    private val timer = RecordTimer()

    fun startRecording() {

        if (file.exists()) {
            file.delete()
        }

        recorderCallback.onVoiceVisibilityChanged(true)
        timer.start()
        recorder = MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
                vibrate()
            } catch (e: Exception) {
                recorderCallback.onVoiceError(e.message ?: "null")
            }
        }
    }

    fun stopRecording(cancelled: Boolean = false) {
        recorderCallback.onVoiceVisibilityChanged(false)
        timer.cancel()
        val successfully = try {
            recorder?.apply {
                stop()
                release()
            }
            true
        } catch (e: Exception) {
            L.tag("voice recorder")
                    .throwable(e)
                    .log("stopping error")
            false
        }
        recorder = null
        if (timer.lastDuration >= RECORD_MIN_DURATION && !cancelled && successfully) {
            recorderCallback.onVoiceRecorded(file.absolutePath)
        }
    }

    private fun vibrate() = VibrationHelper.vibrateHaptic()

    companion object {

        const val AMPLITUDE_UPDATE_DELAY = 50L

        private const val RECORD_NAME = "voice.amr"
        private const val RECORD_MIN_DURATION = 1
        private const val IMPLICIT_DURATION = 60 * 15 * 1000L // 15 minutes
        private const val MAX_AMPLITUDE = 16384
    }

    interface RecorderCallback {
        fun onAmplitudeChanged(amplitude: Float)
        fun onVoiceVisibilityChanged(visible: Boolean)
        fun onVoiceTimeUpdated(time: Int)
        fun onVoiceRecorded(fileName: String)
        fun onVoiceError(error: String)
    }

    private inner class RecordTimer : CountDownTimer(IMPLICIT_DURATION, AMPLITUDE_UPDATE_DELAY) {

        var lastDuration = -1
            private set

        override fun onFinish() {
            stopRecording()
        }

        override fun onTick(millisUntilFinished: Long) {
            val spent = ((IMPLICIT_DURATION - millisUntilFinished) / 1000).toInt()
            if (lastDuration != spent) {
                lastDuration = spent
                recorderCallback.onVoiceTimeUpdated(spent)
            }

            var amplitude = (recorder?.maxAmplitude ?: 0).toFloat() / MAX_AMPLITUDE
            if (amplitude > 1) amplitude = 1f
            recorderCallback.onAmplitudeChanged(sqrt(amplitude)) // amplify weak
        }
    }
}