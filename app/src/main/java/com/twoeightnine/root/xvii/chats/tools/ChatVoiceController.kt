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

package com.twoeightnine.root.xvii.chats.tools

import android.content.Context
import android.os.CountDownTimer
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.VibrationHelper
import global.msnthrp.utils.voice.MaskingVoiceRecorder
import global.msnthrp.utils.voice.MediaVoiceRecorder
import global.msnthrp.utils.voice.VoiceRecorder
import java.io.File
import kotlin.math.sqrt

/**
 * Created by twoeightnine on 1/18/18.
 */

class ChatVoiceController(
        context: Context,
        private val recorderCallback: RecorderCallback
) {

    private var recorder: VoiceRecorder? = null
    private val file = File(context.cacheDir, RECORD_NAME)
    private val timer = RecordTimer()

    fun startRecording() {

        if (file.exists()) {
            file.delete()
        }

        recorderCallback.onVoiceVisibilityChanged(true)
        timer.start()
        val recorder = createVoiceRecorder()
        this.recorder = recorder

        recorder.setupRecorder(file)
        try {
            recorder.start()
            vibrate()
        } catch (e: Exception) {
            recorderCallback.onVoiceError(e.message ?: "null")
        }

    }

    fun stopRecording(cancelled: Boolean = false) {
        recorderCallback.onVoiceVisibilityChanged(false)
        timer.cancel()
        var success = false

        recorder?.doOnRecordReady {
            if (timer.lastDuration >= RECORD_MIN_DURATION && !cancelled && success) {
                recorderCallback.onVoiceRecorded(file.absolutePath)
            }
        }

        success = try {
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
    }

    private fun vibrate() = VibrationHelper.vibrateHaptic()

    private fun createVoiceRecorder(): VoiceRecorder {
        return when {
            Prefs.maskVoice -> MaskingVoiceRecorder()
            else -> MediaVoiceRecorder()
        }
    }

    companion object {

        const val AMPLITUDE_UPDATE_DELAY = 50L

        private const val RECORD_NAME = "voice.wav"
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

            val realAmplitude = recorder?.getMaxAmplitude() ?: 0f
            var amplitude = realAmplitude / MAX_AMPLITUDE
            if (amplitude > 1) amplitude = 1f
            recorderCallback.onAmplitudeChanged(sqrt(amplitude)) // amplify weak
        }
    }
}