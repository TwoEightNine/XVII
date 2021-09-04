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

package global.msnthrp.utils.voice

import android.media.MediaRecorder
import java.io.File

class MediaVoiceRecorder : VoiceRecorder {

    private val recorder = MediaRecorder()

    override fun setupRecorder(outputFile: File) {
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            setOutputFile(outputFile.absolutePath)
        }
    }

    override fun start() {
        recorder.apply {
            prepare()
            start()
        }
    }

    override fun stop() {
        recorder.stop()
    }

    override fun release() {
        recorder.release()
    }

    override fun doOnRecordReady(onReady: () -> Unit) {
        onReady()
    }

    override fun getMaxAmplitude(): Float {
        return try {
            recorder.maxAmplitude.toFloat()
        } catch (e: Exception) {
            0f
        }
    }
}