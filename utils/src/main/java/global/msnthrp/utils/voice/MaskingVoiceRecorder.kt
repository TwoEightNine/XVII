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

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import com.twoeightnine.root.xvii.lg.L
import global.msnthrp.utils.wav.WavFile
import org.apache.commons.math3.transform.DctNormalization
import org.apache.commons.math3.transform.FastCosineTransformer
import org.apache.commons.math3.transform.TransformType
import java.io.File
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.*
import kotlin.math.roundToInt


class MaskingVoiceRecorder : VoiceRecorder {

    private var audioRecord: AudioRecord? = null
    private var isReading = false

    private var file: File? = null
    private val frames = ByteArray(MAX_FRAMES)
    private var totalRead = 0

    private var onReady: (() -> Unit)? = null

    init {
        val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO
        val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT

        val minInternalBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                channelConfig, audioFormat)
        val internalBufferSize = minInternalBufferSize * 4

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, channelConfig, audioFormat, internalBufferSize)
    }

    override fun setupRecorder(outputFile: File) {
        file = outputFile
    }

    override fun start() {
        audioRecord?.startRecording()
        readStart()
    }

    override fun stop() {
        readStop()
        audioRecord?.stop()
    }

    override fun doOnRecordReady(onReady: () -> Unit) {
        this.onReady = onReady
    }

    override fun release() {
        isReading = false
        audioRecord?.release()
    }

    override fun getMaxAmplitude(): Float = 0f

    private fun readStart() {
        isReading = true
        Thread {
            val audioRecord = audioRecord ?: return@Thread

            val myBuffer = ByteArray(BUFFER_SIZE)
            var readCount: Int

            while (isReading) {
                readCount = audioRecord.read(myBuffer, 0, BUFFER_SIZE)
                totalRead += readCount
                for (i in 0 until BUFFER_SIZE) {
                    frames[i + totalRead] = myBuffer[i]
                }
            }
            onStopped()
        }.start()
    }

    private fun readStop() {
        isReading = false
    }

    private fun onStopped() {
        L.tag("voice").log("read $totalRead bytes")
        val intFrames = IntArray(totalRead / 2)
        for (i in intFrames.indices) {
            intFrames[i] = ByteBuffer.wrap(byteArrayOf(frames[2 * i + 1], frames[2 * i])).short.toInt()
        }

        val frames = SpeechMasker.mask(intFrames)

        WavFile.newWavFile(file, 1, frames.size.toLong(), SAMPLE_SIZE * 8, SAMPLE_RATE.toLong())
                .writeFrames(frames, frames.size)

        Handler(Looper.getMainLooper()).post {
            onReady?.invoke()
        }
    }

    companion object {

        private const val SAMPLE_SIZE = 2
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 8192
        private const val MAX_DURATION = 15 * 60 // 15 minutes

        private const val MAX_FRAMES = SAMPLE_RATE * SAMPLE_SIZE * MAX_DURATION

    }

    object SpeechMasker {

        fun mask(frames: IntArray): IntArray {
            val batchSize = 4097
            val offsetMin = 32
            val offsetMax = 128

            val random = SecureRandom()
            val newFrames = performBatchDctTransform(frames, batchSize) { spec ->
                val offset = random.nextInt(offsetMax - offsetMin + 1) + offsetMin
                for (i in offset until batchSize) {
                    spec[i - offset] = spec[i]
                }
                Arrays.fill(spec, batchSize - offset, batchSize, 0.0)
            }
            fixBatchBorders(newFrames, batchSize, radius = 10)

            return newFrames
        }

        private fun performBatchDctTransform(frames: IntArray, batchSize: Int, doOnBatchSpec: (DoubleArray) -> Unit): IntArray {
            val newFrames = IntArray(frames.size)
            val dct = FastCosineTransformer(DctNormalization.ORTHOGONAL_DCT_I)
            for (iBatch in 0 until frames.size / batchSize) {
                val st = iBatch * batchSize
                val end = (iBatch + 1) * batchSize

                val batch = frames.copyOfRange(st, end)
                        .map { it.toDouble() }
                        .toDoubleArray()
                val spec = dct.transform(batch, TransformType.FORWARD)

                doOnBatchSpec(spec)

                val newBatch = dct.transform(spec, TransformType.INVERSE)
                for (i in newBatch.indices) {
                    newFrames[st + i] = newBatch[i].roundToInt()
                }
            }
            return newFrames
        }

        private fun fixBatchBorders(frames: IntArray, batchSize: Int, radius: Int) {
            for (iBatch in 0 until frames.size / batchSize) {
                val border = batchSize * (iBatch + 1)
                val from = frames[border - radius - 1]
                val to = frames[border + radius + 1]

                val step = (to - from) / (2 * radius)
                var now = from
                for (pos in border - radius until border + radius) {
                    frames[pos] = now
                    now += step
                }
            }
        }

    }

}