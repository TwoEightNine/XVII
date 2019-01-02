package com.twoeightnine.root.xvii.chats.fragments

import android.media.MediaRecorder
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.Titleable
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Doc
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.subscribeSmart
import com.twoeightnine.root.xvii.views.LoaderView
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

class VoiceRecordFragment: BaseFragment(), Titleable {

    companion object {
        fun newInstance(listener: (Doc) -> Unit): VoiceRecordFragment {
            val frag = VoiceRecordFragment()
            frag.listener = listener
            return frag
        }
    }

    override fun getTitle() = getString(R.string.voice_message)

    @Inject
    lateinit var api: ApiService

    private val duration = 60000L
    private val warnOver = 5000L

    @BindView(R.id.rlContainer)
    lateinit var rlContainer: RelativeLayout
    @BindView(R.id.ivMic)
    lateinit var ivMic: ImageView
    @BindView(R.id.ivSend)
    lateinit var ivSend: ImageView
    @BindView(R.id.tvTime)
    lateinit var tvTime: TextView
    @BindView(R.id.loader)
    lateinit var loader: LoaderView

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        App.appComponent?.inject(this)
        ivSend.setOnClickListener({
            stopRecording()
            onDoneRecording()
        })

        Style.forViewGroupColor(rlContainer)
        startRecording()
    }

    override fun getLayout() = R.layout.fragment_voice_message

    private var recorder: MediaRecorder? = null
    private var fileName: String? = null
    private var almostOver = false
    var listener: ((Doc) -> Unit)? = null
    private var timer: CountDownTimer = object : CountDownTimer(duration, 1000) {
        override fun onTick(l: Long) {
            tvTime.text = cntDnToTime(l)
            if (!almostOver && l < warnOver) {
                almostOver = true
                tvTime.setTextColor(ContextCompat.getColor(safeContext, R.color.error))
            }
        }

        override fun onFinish() {
            stopRecording()
            onDoneRecording()
        }
    }

    private fun onDoneRecording() {
        api.getDocUploadServer("audio_message")
                .subscribeSmart({
                    uploadDoc(it.uploadUrl!!)
                }, {
                    Lg.wtf("getting upload server error: $it")
                    showError(activity, it)
                })
    }

    private fun uploadDoc(url: String) {
        val file = File(fileName)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        api.uploadDoc(url, body)
                .compose(applySchedulers())
                .subscribe({
                    response ->
                    saveDoc(response.file!!)
                }, {
                    Lg.wtf("uploading error: $it")
                    showError(activity, it.message ?: "null")
                })
    }

    private fun saveDoc(file: String) {
        api.saveDoc(file)
                .subscribeSmart({
                    response ->
                    listener?.invoke(response[0])
                }, {
                    error ->
                    Lg.wtf("saving voice error: $error")
                    showError(activity, error)
                })
    }

    private fun startRecording() {
        timer.start()
        val anim = AnimationUtils.loadAnimation(context, R.anim.voice)
        ivMic.startAnimation(anim)

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
                showError(context, e.message ?: "null")
            }
        }

    }

    private fun stopRecording() {
        timer.cancel()
        ivSend.visibility = View.GONE
        loader.visibility = View.VISIBLE
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

    }

    private fun cntDnToTime(tick: Long): String {
        val sec = ((duration - tick) / 1000).toInt()
        return "00:" + if (sec < 10) "0" + sec else sec
    }

    override fun onStop() {
        super.onStop()
        stopRecording()
    }

}